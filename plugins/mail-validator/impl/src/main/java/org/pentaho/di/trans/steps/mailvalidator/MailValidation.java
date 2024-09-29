/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.mailvalidator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.apache.commons.validator.GenericValidator;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;

public class MailValidation {

  private static Class<?> PKG = MailValidatorMeta.class; // for i18n purposes, needed by Translator2!!

  public static boolean isRegExValid( String emailAdress ) {
    return GenericValidator.isEmail( emailAdress );
  }

  /**
   * verify if there is a mail server registered to the domain name. and return the email servers count
   */
  public static int mailServersCount( String hostName ) throws NamingException {
    Hashtable<String, String> env = new Hashtable<String, String>();
    env.put( "java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory" );
    DirContext ictx = new InitialDirContext( env );
    Attributes attrs = ictx.getAttributes( hostName, new String[] { "MX" } );
    Attribute attr = attrs.get( "MX" );
    if ( attr == null ) {
      return ( 0 );
    }
    return ( attr.size() );
  }

  private static String className() {
    return BaseMessages.getString( PKG, "MailValidator.ClassName" );
  }

  private static int hear( BufferedReader in ) throws IOException {
    String line = null;
    int res = 0;

    while ( ( line = in.readLine() ) != null ) {
      String pfx = line.substring( 0, 3 );
      try {
        res = Integer.parseInt( pfx );
      } catch ( Exception ex ) {
        res = -1;
      }
      if ( line.charAt( 3 ) != '-' ) {
        break;
      }
    }

    return res;
  }

  private static void say( BufferedWriter wr, String text ) throws IOException {
    wr.write( text + "\r\n" );
    wr.flush();

    return;
  }

  private static ArrayList<String> getMX( String hostName ) throws NamingException {
    // Perform a DNS lookup for MX records in the domain
    Hashtable<String, String> env = new Hashtable<String, String>();
    env.put( "java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory" );
    DirContext ictx = new InitialDirContext( env );
    Attributes attrs = ictx.getAttributes( hostName, new String[] { "MX" } );
    Attribute attr = attrs.get( "MX" );

    // if we don't have an MX record, try the machine itself
    if ( ( attr == null ) || ( attr.size() == 0 ) ) {
      attrs = ictx.getAttributes( hostName, new String[] { "A" } );
      attr = attrs.get( "A" );
      if ( attr == null ) {
        throw new NamingException( BaseMessages.getString( PKG, "MailValidator.NoMatchName", hostName ) );
      }
    }

    // Huzzah! we have machines to try. Return them as an array list
    // NOTE: We SHOULD take the preference into account to be absolutely
    // correct. This is left as an exercise for anyone who cares.
    ArrayList<String> res = new ArrayList<String>();
    NamingEnumeration<?> en = attr.getAll();

    while ( en.hasMore() ) {
      String x = (String) en.next();
      String[] f = x.split( " " );
      if ( f[1].endsWith( "." ) ) {
        f[1] = f[1].substring( 0, ( f[1].length() - 1 ) );
      }
      res.add( f[1] );
    }
    return res;
  }

  /**
   * Validate an email address This code is from : http://www.rgagnon.com/javadetails/java-0452.html
   *
   * @param email
   *          address
   * @param sender
   *          email address
   * @param default SMTP Server
   * @param timeout
   *          for socket connection
   * @param deepCheck
   *          (if we want to perform a SMTP check
   * @return true or false
   */
  public static MailValidationResult isAddressValid( LogChannelInterface log, String address,
    String senderAddress, String defaultSMTPServer, int timeout, boolean deepCheck ) {

    MailValidationResult result = new MailValidationResult();

    if ( !isRegExValid( address ) ) {
      result.setErrorMessage( BaseMessages.getString( PKG, "MailValidator.MalformedAddress", address ) );
      return result;
    }

    // Find the separator for the domain name
    int pos = address.indexOf( '@' );

    // If the address does not contain an '@', it's not valid
    if ( pos == -1 ) {
      return result;
    }

    if ( !deepCheck ) {
      result.setValide( true );
      return result;
    }

    // Isolate the domain/machine name and get a list of mail exchangers
    String domain = address.substring( ++pos );

    // Maybe user want to switch to a default SMTP server?
    // In that case, we will ignore the domain
    // extracted from email address

    ArrayList<String> mxList = new ArrayList<String>();
    if ( Utils.isEmpty( defaultSMTPServer ) ) {
      try {
        mxList = getMX( domain );

        // Just because we can send mail to the domain, doesn't mean that the
        // address is valid, but if we can't, it's a sure sign that it isn't
        if ( mxList == null || mxList.size() == 0 ) {
          result.setErrorMessage( BaseMessages.getString( PKG, "MailValidator.NoMachinesInDomain", domain ) );
          return result;
        }
      } catch ( Exception ex ) {
        result.setErrorMessage( BaseMessages.getString( PKG, "MailValidator.ErrorGettingMachinesInDomain", ex
          .getMessage() ) );
        return result;
      }
    } else {
      mxList.add( defaultSMTPServer );
    }

    if ( log.isDebug() ) {
      log.logDebug( BaseMessages.getString( PKG, "MailValidator.ExchangersFound", "" + mxList.size() ) );
    }

    // Now, do the SMTP validation, try each mail exchanger until we get
    // a positive acceptance. It *MAY* be possible for one MX to allow
    // a message [store and forwarder for example] and another [like
    // the actual mail server] to reject it. This is why we REALLY ought
    // to take the preference into account.
    for ( int mx = 0; mx < mxList.size(); mx++ ) {
      boolean valid = false;
      BufferedReader rdr = null;
      BufferedWriter wtr = null;
      Socket skt = null;
      try {
        String exhanger = mxList.get( mx );
        if ( log.isDebug() ) {
          log.logDebug( className(), BaseMessages.getString( PKG, "MailValidator.TryingExchanger", exhanger ) );
        }

        int res;

        skt = new Socket( exhanger, 25 );
        // set timeout (milliseconds)
        if ( timeout > 0 ) {
          skt.setSoTimeout( timeout );
        }

        if ( log.isDebug() ) {
          log.logDebug( className(), BaseMessages.getString(
            PKG, "MailValidator.ConnectingTo", exhanger, "25", skt.isConnected() + "" ) );
        }

        rdr = new BufferedReader( new InputStreamReader( skt.getInputStream() ) );
        wtr = new BufferedWriter( new OutputStreamWriter( skt.getOutputStream() ) );

        res = hear( rdr );
        if ( res != 220 ) {
          throw new Exception( BaseMessages.getString( PKG, "MailValidator.InvalidHeader" ) );
        }

        // say HELLO it's me
        if ( log.isDebug() ) {
          log.logDebug( className(), BaseMessages.getString( PKG, "MailValidator.SayHello", domain ) );
        }
        say( wtr, "EHLO " + domain );
        res = hear( rdr );
        if ( res != 250 ) {
          throw new Exception( "Not ESMTP" );
        }
        if ( log.isDebug() ) {
          log.logDebug( className(), BaseMessages.getString( PKG, "MailValidator.ServerReplied", "" + res ) );
        }

        // validate the sender address
        if ( log.isDebug() ) {
          log.logDebug( className(), BaseMessages.getString( PKG, "MailValidator.CheckSender", senderAddress ) );
        }
        say( wtr, "MAIL FROM: <" + senderAddress + ">" );
        res = hear( rdr );
        if ( res != 250 ) {
          throw new Exception( BaseMessages.getString( PKG, "MailValidator.SenderRejected" ) );
        }
        if ( log.isDebug() ) {
          log.logDebug( className(), BaseMessages.getString( PKG, "MailValidator.SenderAccepted", "" + res ) );
        }

        // Validate receiver
        if ( log.isDebug() ) {
          log.logDebug( className(), BaseMessages.getString( PKG, "MailValidator.CheckReceiver", address ) );
        }
        say( wtr, "RCPT TO: <" + address + ">" );
        res = hear( rdr );

        // be polite
        say( wtr, "RSET" );
        hear( rdr );
        say( wtr, "QUIT" );
        hear( rdr );
        if ( res != 250 ) {
          throw new Exception( BaseMessages.getString( PKG, "MailValidator.AddressNotValid", address ) );
        }

        if ( log.isDebug() ) {
          log.logDebug( className(), BaseMessages.getString( PKG, "MailValidator.ReceiverAccepted", address, ""
            + res ) );
        }
        valid = true;

      } catch ( Exception ex ) {
        // Do nothing but try next host
        result.setValide( false );
        result.setErrorMessage( ex.getMessage() );
      } finally {
        if ( rdr != null ) {
          try {
            rdr.close();
          } catch ( Exception e ) {
            // ignore this
          }
        }
        if ( wtr != null ) {
          try {
            wtr.close();
          } catch ( Exception e ) {
            // ignore this
          }
        }
        if ( skt != null ) {
          try {
            skt.close();
          } catch ( Exception e ) {
            // ignore this
          }
        }

        if ( valid ) {
          result.setValide( true );
          result.setErrorMessage( null );
          if ( log.isDebug() ) {
            log.logDebug( className(), "=============================================" );
          }
          return result;
        }
      }
    }
    if ( log.isDebug() ) {
      log.logDebug( className(), "=============================================" );
    }

    return result;
  }

}
