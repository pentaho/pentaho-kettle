/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019-2023 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.plugins.fileopensave.api.providers;

import java.util.Arrays;
import java.util.List;

/**
 * Created by bmorrise on 2/23/19.
 */
public class Utils {

  public static List<String>
    validExtensions = Arrays
    .asList( "3g2", "3ga", "3gp", "7z", "aa", "aac", "ac", "accdb", "accdt", "ace", "adn", "ai", "aif", "aifc", "aiff",
      "ait", "amr", "ani", "apk", "app", "applescript", "asax", "asc", "ascx", "asf", "ash", "ashx", "asm", "asmx",
      "asp", "aspx", "asx", "au", "aup", "avi", "axd", "aze", "bak", "bash", "bat", "bin", "blank", "bmp", "bowerrc",
      "bpg", "browser", "bz2", "bzempty", "c", "cab", "cad", "caf", "cal", "catalog", "cd", "cdda", "cer", "cfg", "cfm",
      "cfml", "cgi", "chm", "class", "cmd", "code-workspace", "codekit", "coffee", "coffeelintignore", "com", "compile",
      "conf", "config", "cpp", "cptx", "cr2", "crdownload", "crt", "crypt", "cs", "csh", "cson", "csproj", "css", "csv",
      "cue", "cur", "dart", "dat", "data", "db", "dbf", "deb", "default", "dgn", "dist", "diz", "dll", "dmg", "dng",
      "doc", "docb", "docm", "docx", "dot", "dotm", "dotx", "download", "dpj", "ds_store", "dsn", "dtd", "dwg", "dxf",
      "editorconfig", "el", "elf", "eml", "enc", "eot", "eps", "epub", "eslintignore", "exe", "f4v", "fax", "fb2",
      "fla", "flac", "flv", "fnt", "folder", "fon", "gadget", "gdp", "gem", "gif", "gitattributes", "gitignore", "go",
      "gpg", "gpl", "gradle", "gz", "h", "handlebars", "hbs", "heic", "hlp", "hs", "hsl", "htm", "html", "ibooks",
      "icns", "ico", "ics", "idx", "iff", "ifo", "image", "img", "iml", "in", "inc", "indd", "inf", "info", "ini",
      "inv", "iso", "j2", "jar", "java", "jpe", "jpeg", "jpg", "js", "json", "jsp", "jsx", "key", "kf8", "kjb", "kmk",
      "ksh", "kt", "ktr", "kts", "kup", "less", "lex", "licx", "lisp", "lit", "lnk", "lock", "log", "lua", "m", "m2v",
      "m3u", "m3u8", "m4", "m4a", "m4r", "m4v", "map", "master", "mc", "md", "mdb", "mdf", "me", "mi", "mid", "midi",
      "mk", "mkv", "mm", "mng", "mo", "mobi", "mod", "mov", "mp2", "mp3", "mp4", "mpa", "mpd", "mpe", "mpeg", "mpg",
      "mpga", "mpp", "mpt", "msg", "msi", "msu", "nef", "nes", "nfo", "nix", "npmignore", "ocx", "odb", "ods", "odt",
      "ogg", "ogv", "ost", "otf", "ott", "ova", "ovf", "p12", "p7b", "pages", "part", "pcd", "pdb", "pdf", "pem", "pfx",
      "pgp", "ph", "phar", "php", "pid", "pkg", "pl", "plist", "pm", "png", "po", "pom", "pot", "potx", "pps", "ppsx",
      "ppt", "pptm", "pptx", "prop", "ps", "ps1", "psd", "psp", "pst", "pub", "py", "pyc", "qt", "ra", "ram", "rar",
      "raw", "rb", "rdf", "rdl", "reg", "resx", "retry", "rm", "rom", "rpm", "rpt", "rsa", "rss", "rst", "rtf", "ru",
      "rub", "sass", "scss", "sdf", "sed", "sh", "sit", "sitemap", "skin", "sldm", "sldx", "sln", "sol", "sphinx",
      "sql", "sqlite", "step", "stl", "svg", "swd", "swf", "swift", "swp", "sys", "tar", "tax", "tcsh", "tex",
      "tfignore", "tga", "tgz", "tif", "tiff", "tmp", "tmx", "torrent", "tpl", "ts", "tsv", "ttf", "twig", "txt", "udf",
      "vb", "vbproj", "vbs", "vcd", "vcf", "vcs", "vdi", "vdx", "vmdk", "vob", "vox", "vscodeignore", "vsd", "vss",
      "vst", "vsx", "vtx", "war", "wav", "wbk", "webinfo", "webm", "webp", "wma", "wmf", "wmv", "woff", "woff2", "wps",
      "wsf", "xaml", "xcf", "xfl", "xlm", "xls", "xlsm", "xlsx", "xlt", "xltm", "xltx", "xml", "xpi", "xps", "xrb",
      "xsd", "xsl", "xspf", "xz", "yaml", "yml", "z", "zip", "zsh" );

  public static boolean matches( String name, String filters ) {
    return filters == null || name.matches( filters.replace( ".", ".*\\." ) );
  }

  public static String getExtension( String path ) {
    int dotPos = path.lastIndexOf( "." );
    if ( dotPos == -1 || dotPos < path.lastIndexOf( "/" ) ) {
      return "";
    }
    return path.substring( path.lastIndexOf( "." ) + 1, path.length() );
  }

  public static boolean isValidExtension( String extension ) {
    return validExtensions.contains( extension );
  }

  public static String getParent( String path, String separator ) {
    return path.substring( 0, path.lastIndexOf( separator) );
  }

  public static String getName( String path, String separator ) {
    return path.substring( path.lastIndexOf( separator ), path.length() );
  }
}
