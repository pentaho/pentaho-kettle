/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/lgpl-2.0.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2008 Bayon Technologies, Inc.  All rights reserved. 
 * Copyright (C) 2004 The jTDS Project
 */
package org.pentaho.di.jdbc;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.pentaho.di.i18n.BaseMessages;


public class DateTime {
  
    private static Class<?> PKG = KettleDriver.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    /** Per thread instance of Calendar used for conversions. */
    private static ThreadLocal<Object> calendar = new ThreadLocal<Object>() {
        protected synchronized Object initialValue() {
            return new GregorianCalendar();
        }
    };
    /** Indicates date value not used. */
    static final int DATE_NOT_USED = Integer.MIN_VALUE;
    /** Indicates time value not used. */
    static final int TIME_NOT_USED = Integer.MIN_VALUE;
    /** The date component of the server datetime value. */
    private int   date;
    /** The time component of the server datetime value. */
    private int   time;
    /** Unpacked year value. */
    private short year;
    /** Unpacked month value. */
    private short month;
    /** Unpacked day value. */
    private short day;
    /** Unpacked hour value. */
    private short hour;
    /** Unpacked minute value. */
    private short minute;
    /** Unpacked second value. */
    private short second;
    /** Unpacked millisecond value. */
    private short millis;
    /** Indicates server datetime values have been unpacked. */
    private boolean unpacked;
    /** Cached value of the datetime as a <code>String</code>. */
    private String    stringValue;
    /** Cached value of the datetime as a <code>java.sql.Timestamp</code>. */
    private Timestamp tsValue;
    /** Cached value of the datetime as a <code>java.sql.Date</code>. */
    private Date      dateValue;
    /** Cached value of the datetime as a <code>java.sql.Time</code>. */
    private Time      timeValue;

    /**
     * Constructs a DateTime object from the two integer components of a
     * datetime.
     *
     * @param date server date field
     * @param time server time field
     */
    DateTime(int date, int time) {
        this.date = date;
        this.time = time;
    }

    /**
     * Constructs a DateTime object from the two short components of a
     * smalldatetime.
     *
     * @param date server date field
     * @param time server time field
     */
    DateTime(short date, short time) {
        this.date = (int) date & 0xFFFF;
        this.time = (int) time * 60 * 300;
    }

    /**
     * Constructs a DateTime object from a <code>java.sql.Timestamp</code>.
     *
     * @param ts <code>Timestamp</code> object representing the datetime
     * @throws SQLException if the date is out of range
     */
    DateTime(Timestamp ts) throws SQLException {
        tsValue = ts;
        GregorianCalendar cal = (GregorianCalendar)calendar.get();
        cal.setTime((java.util.Date) ts);

       
        this.year   = (short)cal.get(Calendar.YEAR);
        this.month  = (short)(cal.get(Calendar.MONTH) + 1);
        this.day    = (short)cal.get(Calendar.DAY_OF_MONTH);
        this.hour   = (short)cal.get(Calendar.HOUR_OF_DAY);
        this.minute = (short)cal.get(Calendar.MINUTE);
        this.second = (short)cal.get(Calendar.SECOND);
        this.millis = (short)cal.get(Calendar.MILLISECOND);
        packDate();
        packTime();
        unpacked = true;
    }

    /**
     * Constructs a DateTime object from a <code>java.sql.Time</code>.
     *
     * @param t <code>Time</code> object representing the datetime
     */
    DateTime(Time t) {
        timeValue = t;
        GregorianCalendar cal = (GregorianCalendar)calendar.get();
        cal.setTime((java.util.Date) t);
        this.date   = DATE_NOT_USED;
        this.year   = 1900;
        this.month  = 1;
        this.day    = 1;
        this.hour   = (short)cal.get(Calendar.HOUR_OF_DAY);
        this.minute = (short)cal.get(Calendar.MINUTE);
        this.second = (short)cal.get(Calendar.SECOND);
        this.millis = (short)cal.get(Calendar.MILLISECOND);
        packTime();
        this.year  = 1970;
        this.month = 1;
        this.day   = 1;
        unpacked   = true;
    }

    /**
     * Constructs a DateTime object from a <code>java.sql.Date</code>.
     *
     * @param d <code>Date</code> object representing the datetime
     * @throws SQLException if the Date is out of range
     */
    DateTime(Date d) throws SQLException {
        dateValue = d;
        GregorianCalendar cal = (GregorianCalendar)calendar.get();
        cal.setTime((java.util.Date) d);
        this.year   = (short)cal.get(Calendar.YEAR);
        this.month  = (short)(cal.get(Calendar.MONTH) + 1);
        this.day    = (short)cal.get(Calendar.DAY_OF_MONTH);
        this.hour   = 0;
        this.minute = 0;
        this.second = 0;
        this.millis = 0;
        packDate();
        this.time = TIME_NOT_USED;
        unpacked  = true;
    }

    /**
     * Retrieves the date component of a datetime value.
     *
     * @return the datetime date component as an <code>int</code>
     */
    int getDate() {
        return (date == DATE_NOT_USED) ? 0 : date;
    }

    /**
     * Retrieves the time component of a datetime value.
     *
     * @return the datetime time component as an <code>int</code>
     */
    int getTime() {
        return (time == TIME_NOT_USED) ? 0 : time;
    }

    /**
     * Converts a Julian datetime from the Sybase epoch of 1900-01-01 to the
     * equivalent unpacked year/month/day etc.
     *
     * Algorithm  from Fliegel, H F and van Flandern, T C (1968).
     * Communications of the ACM, Vol 11, No 10 (October, 1968).
     * <pre>
     *           SUBROUTINE GDATE (JD, YEAR,MONTH,DAY)
     *     C
     *     C---COMPUTES THE GREGORIAN CALENDAR DATE (YEAR,MONTH,DAY)
     *     C   GIVEN THE JULIAN DATE (JD).
     *     C
     *           INTEGER JD,YEAR,MONTH,DAY,I,J,K
     *     C
     *           L= JD+68569
     *           N= 4*L/146097
     *           L= L-(146097*N+3)/4
     *           I= 4000*(L+1)/1461001
     *           L= L-1461*I/4+31
     *           J= 80*L/2447
     *           K= L-2447*J/80
     *           L= J/11
     *           J= J+2-12*L
     *           I= 100*(N-49)+I+L
     *     C
     *           YEAR= I
     *           MONTH= J
     *           DAY= K
     *     C
     *           RETURN
     *           END
     * </pre>
     */
    private void unpackDateTime() {
        if (this.date == DATE_NOT_USED) {
            this.year  = 1970;
            this.month = 1;
            this.day   = 1;
        } else {
            if (date == 0) {
                // Optimize common case of 1900-01-01 which is used as
                // the default date for datetimes where only the time is set.
                this.year  = 1900;
                this.month = 1;
                this.day   = 1;
            } else {
                int l = date + 68569 + 2415021;
                int n = 4 * l / 146097;
                l = l - (146097 * n + 3) / 4;
                int i = 4000 * (l + 1) / 1461001;
                l = l - 1461 * i / 4 + 31;
                int j = 80 * l / 2447;
                int k = l - 2447 * j / 80;
                l = j / 11;
                j = j + 2 - 12 * l;
                i = 100 * (n - 49) + i + l;
                this.year  = (short)i;
                this.month = (short)j;
                this.day   = (short)k;
            }
        }
        if (time == TIME_NOT_USED) {
            this.hour   = 0;
            this.minute = 0;
            this.second = 0;
        } else {
            int hours = time / 1080000;
            time = time - hours * 1080000;
            int minutes = time / 18000;
            time = time - (minutes * 18000);
            int seconds = time / 300;
            time = time - seconds * 300;
            time = (int) Math.round(time * 1000 / 300f);
            this.hour = (short)hours;
            this.minute = (short)minutes;
            this.second = (short)seconds;
            this.millis = (short)time;
        }
        unpacked = true;
    }

    /**
     * Converts a calendar date into days since 1900 (Sybase epoch).
     * <p>
     * Algorithm from Fliegel, H F and van Flandern, T C (1968).
     * Communications of the ACM, Vol 11, No 10 (October, 1968).
     *
     * <pre>
     *           INTEGER FUNCTION JD (YEAR,MONTH,DAY)
     *     C
     *     C---COMPUTES THE JULIAN DATE (JD) GIVEN A GREGORIAN CALENDAR
     *     C   DATE (YEAR,MONTH,DAY).
     *     C
     *           INTEGER YEAR,MONTH,DAY,I,J,K
     *     C
     *           I= YEAR
     *           J= MONTH
     *           K= DAY
     *     C
     *           JD= K-32075+1461*(I+4800+(J-14)/12)/4+367*(J-2-(J-14)/12*12)
     *          2    /12-3*((I+4900+(J-14)/12)/100)/4
     *     C
     *           RETURN
     *           END
     * </pre>
     *
     * @throws java.sql.SQLException if the date is outside the accepted range, 1753-9999
     */
    public void packDate() throws SQLException {
        if (year < 1753 || year > 9999) {
            throw new SQLException(BaseMessages.getString(PKG, "error.datetime.range"), "22003");
        }
        date = day - 32075 + 1461 * (year + 4800 + (month - 14) / 12) / 4
                + 367 * (month - 2 - (month - 14) / 12 * 12) / 12
                - 3 * ((year + 4900 + (month -14) / 12) / 100) / 4 - 2415021;
    }

    /**
     * Converts separate time components into a datetime time value.
     */
    public void packTime() {
        time = hour * 1080000;
        time += minute * 18000;
        time += second * 300;
        time += Math.round(millis * 300f / 1000);
        if (time > 25919999) {
            // Time field has overflowed need to increment days
            // Sybase does not allow invalid time component
            time   = 0;
            hour   = 0;
            minute = 0;
            second = 0;
            millis = 0;
            if (date != DATE_NOT_USED) {
                GregorianCalendar cal = (GregorianCalendar)calendar.get();
                cal.set(Calendar.YEAR, this.year);
                cal.set(Calendar.MONTH, this.month - 1);
                cal.set(Calendar.DAY_OF_MONTH, this.day);
                cal.add(Calendar.DATE, 1);
                year   = (short)cal.get(Calendar.YEAR);
                month  = (short)(cal.get(Calendar.MONTH) + 1);
                day    = (short)cal.get(Calendar.DAY_OF_MONTH);
                date++;
            }
        }
    }

    /**
     * Retrieves the current datetime value as a Timestamp.
     *
     * @return the current datetime value as a <code>java.sql.Timestamp</code>
     */
    public Timestamp toTimestamp() {
        if (tsValue == null) {
            if (!unpacked) {
                unpackDateTime();
            }
            GregorianCalendar cal = (GregorianCalendar)calendar.get();
            cal.set(Calendar.YEAR, this.year);
            cal.set(Calendar.MONTH, this.month - 1);
            cal.set(Calendar.DAY_OF_MONTH, this.day);
            cal.set(Calendar.HOUR_OF_DAY, this.hour);
            cal.set(Calendar.MINUTE, this.minute);
            cal.set(Calendar.SECOND, this.second);
            cal.set(Calendar.MILLISECOND, this.millis);
            tsValue = new Timestamp(cal.getTime().getTime());
        }
        return tsValue;
    }

    /**
     * Retrieves the current datetime value as a Date.
     *
     * @return the current datetime value as a <code>java.sql.Date</code>
     */
    public Date toDate() {
        if (dateValue == null) {
            if (!unpacked) {
                unpackDateTime();
            }
            GregorianCalendar cal = (GregorianCalendar)calendar.get();
            cal.set(Calendar.YEAR, this.year);
            cal.set(Calendar.MONTH, this.month - 1);
            cal.set(Calendar.DAY_OF_MONTH, this.day);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            dateValue = new Date(cal.getTime().getTime());
        }
        return dateValue;
    }

    /**
     * Retrieves the current datetime value as a Time.
     *
     * @return the current datetime value as a <code>java.sql.Time</code>
     */
    public Time toTime() {
        if (timeValue == null) {
            if (!unpacked) {
                unpackDateTime();
            }
            GregorianCalendar cal = (GregorianCalendar)calendar.get();
            cal.set(Calendar.YEAR, 1970);
            cal.set(Calendar.MONTH, 0);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.HOUR_OF_DAY, this.hour);
            cal.set(Calendar.MINUTE, this.minute);
            cal.set(Calendar.SECOND, this.second);
            cal.set(Calendar.MILLISECOND, this.millis);
            timeValue = new Time(cal.getTime().getTime());
        }
        return timeValue;
    }

    /**
     * Retrieves the current datetime value as a Time, Date or Timestamp.
     *
     * @return the current datetime value as an <code>java.lang.Object</code>
     */
    public Object toObject() {
        if (date == DATE_NOT_USED) {
            return toTime();
        }
        if (time == TIME_NOT_USED) {
            return toDate();
        }
        return toTimestamp();
    }

    /**
     * Retrieves the current datetime value as a String.
     *
     * @return the current datetime value as a <code>String</code>
     */
    public String toString() {
        if (stringValue == null) {
            if (!unpacked) {
                unpackDateTime();
            }
            //
            // Make local copies to avoid corrupting unpacked
            // components.
            //
            int day    = this.day;
            int month  = this.month;
            int year   = this.year;
            int millis = this.millis;
            int second = this.second;
            int minute = this.minute;
            int hour  = this.hour;
            char buf[] = new char[23];
            int p = 0;
            if (date != DATE_NOT_USED) {
                p = 10;
                buf[--p] = (char)('0' + day % 10);
                day /= 10;
                buf[--p] = (char)('0' + day % 10);
                buf[--p] = '-';
                buf[--p] = (char)('0' + month % 10);
                month /= 10;
                buf[--p] = (char)('0' + month % 10);
                buf[--p] = '-';
                buf[--p] = (char)('0' + year % 10);
                year /= 10;
                buf[--p] = (char)('0' + year % 10);
                year /= 10;
                buf[--p] = (char)('0' + year % 10);
                year /= 10;
                buf[--p] = (char)('0' + year % 10);
                p += 10;
                if (time != TIME_NOT_USED) {
                    buf[p++] = ' ';
                }
            }
            if (time != TIME_NOT_USED) {
                p += 12;
                buf[--p] = (char)('0' + millis % 10);
                millis /= 10;
                buf[--p] = (char)('0' + millis % 10);
                millis /= 10;
                buf[--p] = (char)('0' + millis % 10);
                buf[--p] = '.';
                buf[--p] = (char)('0' + second % 10);
                second /= 10;
                buf[--p] = (char)('0' + second % 10);
                buf[--p] = ':';
                buf[--p] = (char)('0' + minute % 10);
                minute /= 10;
                buf[--p] = (char)('0' + minute % 10);
                buf[--p] = ':';
                buf[--p] = (char)('0' + hour % 10);
                hour /= 10;
                buf[--p] = (char)('0' + hour % 10);
                p += 12;
                if (buf[p-1] == '0') {
                    p--;
                }
                if (buf[p-1] == '0') {
                    p--;
                }
            }
            stringValue = String.valueOf(buf, 0, p);
        }
        return stringValue;
    }
}
