/*******************************************************************************
 * Copyright (c) 2004, 2013 1&1 Internet AG, Germany, http://www.1und1.de,
 *                          EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    1&1 Internet AG and others - original API and implementation
 *    EclipseSource - adaptation for the Eclipse Remote Application Platform
 ******************************************************************************/

/**
 * A Collection of utility functions to escape and unescape strings.
 */
rwt.qx.Class.define( "rwt.html.Entity", {

  statics : {

    /** Mapping of HTML entity names to the corresponding char code */
    TO_CHARCODE : {
      "quot"     : 34, // " - double-quote
      "amp"      : 38, // &
      "lt"       : 60, // <
      "gt"       : 62, // >

      // http://www.w3.org/TR/REC-html40/sgml/entities.html
      // ISO 8859-1 characters
      "nbsp"     : 160, // no-break space
      "iexcl"    : 161, // inverted exclamation mark
      "cent"     : 162, // cent sign
      "pound"    : 163, // pound sterling sign
      "curren"   : 164, // general currency sign
      "yen"      : 165, // yen sign
      "brvbar"   : 166, // broken (vertical) bar
      "sect"     : 167, // section sign
      "uml"      : 168, // umlaut (dieresis)
      "copy"     : 169, // copyright sign
      "ordf"     : 170, // ordinal indicator, feminine
      "laquo"    : 171, // angle quotation mark, left
      "not"      : 172, // not sign
      "shy"      : 173, // soft hyphen
      "reg"      : 174, // registered sign
      "macr"     : 175, // macron
      "deg"      : 176, // degree sign
      "plusmn"   : 177, // plus-or-minus sign
      "sup2"     : 178, // superscript two
      "sup3"     : 179, // superscript three
      "acute"    : 180, // acute accent
      "micro"    : 181, // micro sign
      "para"     : 182, // pilcrow (paragraph sign)
      "middot"   : 183, // middle dot
      "cedil"    : 184, // cedilla
      "sup1"     : 185, // superscript one
      "ordm"     : 186, // ordinal indicator, masculine
      "raquo"    : 187, // angle quotation mark, right
      "frac14"   : 188, // fraction one-quarter
      "frac12"   : 189, // fraction one-half
      "frac34"   : 190, // fraction three-quarters
      "iquest"   : 191, // inverted question mark
      "Agrave"   : 192, // capital A, grave accent
      "Aacute"   : 193, // capital A, acute accent
      "Acirc"    : 194, // capital A, circumflex accent
      "Atilde"   : 195, // capital A, tilde
      "Auml"     : 196, // capital A, dieresis or umlaut mark
      "Aring"    : 197, // capital A, ring
      "AElig"    : 198, // capital AE diphthong (ligature)
      "Ccedil"   : 199, // capital C, cedilla
      "Egrave"   : 200, // capital E, grave accent
      "Eacute"   : 201, // capital E, acute accent
      "Ecirc"    : 202, // capital E, circumflex accent
      "Euml"     : 203, // capital E, dieresis or umlaut mark
      "Igrave"   : 204, // capital I, grave accent
      "Iacute"   : 205, // capital I, acute accent
      "Icirc"    : 206, // capital I, circumflex accent
      "Iuml"     : 207, // capital I, dieresis or umlaut mark
      "ETH"      : 208, // capital Eth, Icelandic
      "Ntilde"   : 209, // capital N, tilde
      "Ograve"   : 210, // capital O, grave accent
      "Oacute"   : 211, // capital O, acute accent
      "Ocirc"    : 212, // capital O, circumflex accent
      "Otilde"   : 213, // capital O, tilde
      "Ouml"     : 214, // capital O, dieresis or umlaut mark
      "times"    : 215, // multiply sign
      "Oslash"   : 216, // capital O, slash
      "Ugrave"   : 217, // capital U, grave accent
      "Uacute"   : 218, // capital U, acute accent
      "Ucirc"    : 219, // capital U, circumflex accent
      "Uuml"     : 220, // capital U, dieresis or umlaut mark
      "Yacute"   : 221, // capital Y, acute accent
      "THORN"    : 222, // capital THORN, Icelandic
      "szlig"    : 223, // small sharp s, German (sz ligature)
      "agrave"   : 224, // small a, grave accent
      "aacute"   : 225, // small a, acute accent
      "acirc"    : 226, // small a, circumflex accent
      "atilde"   : 227, // small a, tilde
      "auml"     : 228, // small a, dieresis or umlaut mark
      "aring"    : 229, // small a, ring
      "aelig"    : 230, // small ae diphthong (ligature)
      "ccedil"   : 231, // small c, cedilla
      "egrave"   : 232, // small e, grave accent
      "eacute"   : 233, // small e, acute accent
      "ecirc"    : 234, // small e, circumflex accent
      "euml"     : 235, // small e, dieresis or umlaut mark
      "igrave"   : 236, // small i, grave accent
      "iacute"   : 237, // small i, acute accent
      "icirc"    : 238, // small i, circumflex accent
      "iuml"     : 239, // small i, dieresis or umlaut mark
      "eth"      : 240, // small eth, Icelandic
      "ntilde"   : 241, // small n, tilde
      "ograve"   : 242, // small o, grave accent
      "oacute"   : 243, // small o, acute accent
      "ocirc"    : 244, // small o, circumflex accent
      "otilde"   : 245, // small o, tilde
      "ouml"     : 246, // small o, dieresis or umlaut mark
      "divide"   : 247, // divide sign
      "oslash"   : 248, // small o, slash
      "ugrave"   : 249, // small u, grave accent
      "uacute"   : 250, // small u, acute accent
      "ucirc"    : 251, // small u, circumflex accent
      "uuml"     : 252, // small u, dieresis or umlaut mark
      "yacute"   : 253, // small y, acute accent
      "thorn"    : 254, // small thorn, Icelandic
      "yuml"     : 255, // small y, dieresis or umlaut mark

      // Latin Extended-B
      "fnof"     : 402, // latin small f with hook = function= florin, U+0192 ISOtech

      // Greek
      "Alpha"    : 913, // greek capital letter alpha, U+0391
      "Beta"     : 914, // greek capital letter beta, U+0392
      "Gamma"    : 915, // greek capital letter gamma,U+0393 ISOgrk3
      "Delta"    : 916, // greek capital letter delta,U+0394 ISOgrk3
      "Epsilon"  : 917, // greek capital letter epsilon, U+0395
      "Zeta"     : 918, // greek capital letter zeta, U+0396
      "Eta"      : 919, // greek capital letter eta, U+0397
      "Theta"    : 920, // greek capital letter theta,U+0398 ISOgrk3
      "Iota"     : 921, // greek capital letter iota, U+0399
      "Kappa"    : 922, // greek capital letter kappa, U+039A
      "Lambda"   : 923, // greek capital letter lambda,U+039B ISOgrk3
      "Mu"       : 924, // greek capital letter mu, U+039C
      "Nu"       : 925, // greek capital letter nu, U+039D
      "Xi"       : 926, // greek capital letter xi, U+039E ISOgrk3
      "Omicron"  : 927, // greek capital letter omicron, U+039F
      "Pi"       : 928, // greek capital letter pi, U+03A0 ISOgrk3
      "Rho"      : 929, // greek capital letter rho, U+03A1

      // there is no Sigmaf, and no U+03A2 character either
      "Sigma"    : 931, // greek capital letter sigma,U+03A3 ISOgrk3
      "Tau"      : 932, // greek capital letter tau, U+03A4
      "Upsilon"  : 933, // greek capital letter upsilon,U+03A5 ISOgrk3
      "Phi"      : 934, // greek capital letter phi,U+03A6 ISOgrk3
      "Chi"      : 935, // greek capital letter chi, U+03A7
      "Psi"      : 936, // greek capital letter psi,U+03A8 ISOgrk3
      "Omega"    : 937, // greek capital letter omega,U+03A9 ISOgrk3
      "alpha"    : 945, // greek small letter alpha,U+03B1 ISOgrk3
      "beta"     : 946, // greek small letter beta, U+03B2 ISOgrk3
      "gamma"    : 947, // greek small letter gamma,U+03B3 ISOgrk3
      "delta"    : 948, // greek small letter delta,U+03B4 ISOgrk3
      "epsilon"  : 949, // greek small letter epsilon,U+03B5 ISOgrk3
      "zeta"     : 950, // greek small letter zeta, U+03B6 ISOgrk3
      "eta"      : 951, // greek small letter eta, U+03B7 ISOgrk3
      "theta"    : 952, // greek small letter theta,U+03B8 ISOgrk3
      "iota"     : 953, // greek small letter iota, U+03B9 ISOgrk3
      "kappa"    : 954, // greek small letter kappa,U+03BA ISOgrk3
      "lambda"   : 955, // greek small letter lambda,U+03BB ISOgrk3
      "mu"       : 956, // greek small letter mu, U+03BC ISOgrk3
      "nu"       : 957, // greek small letter nu, U+03BD ISOgrk3
      "xi"       : 958, // greek small letter xi, U+03BE ISOgrk3
      "omicron"  : 959, // greek small letter omicron, U+03BF NEW
      "pi"       : 960, // greek small letter pi, U+03C0 ISOgrk3
      "rho"      : 961, // greek small letter rho, U+03C1 ISOgrk3
      "sigmaf"   : 962, // greek small letter final sigma,U+03C2 ISOgrk3
      "sigma"    : 963, // greek small letter sigma,U+03C3 ISOgrk3
      "tau"      : 964, // greek small letter tau, U+03C4 ISOgrk3
      "upsilon"  : 965, // greek small letter upsilon,U+03C5 ISOgrk3
      "phi"      : 966, // greek small letter phi, U+03C6 ISOgrk3
      "chi"      : 967, // greek small letter chi, U+03C7 ISOgrk3
      "psi"      : 968, // greek small letter psi, U+03C8 ISOgrk3
      "omega"    : 969, // greek small letter omega,U+03C9 ISOgrk3
      "thetasym" : 977, // greek small letter theta symbol,U+03D1 NEW
      "upsih"    : 978, // greek upsilon with hook symbol,U+03D2 NEW
      "piv"      : 982, // greek pi symbol, U+03D6 ISOgrk3

      // General Punctuation
      "bull"     : 8226, // bullet = black small circle,U+2022 ISOpub

      // bullet is NOT the same as bullet operator, U+2219
      "hellip"   : 8230, // horizontal ellipsis = three dot leader,U+2026 ISOpub
      "prime"    : 8242, // prime = minutes = feet, U+2032 ISOtech
      "Prime"    : 8243, // double prime = seconds = inches,U+2033 ISOtech
      "oline"    : 8254, // overline = spacing overscore,U+203E NEW
      "frasl"    : 8260, // fraction slash, U+2044 NEW

      // Letterlike Symbols
      "weierp"   : 8472, // script capital P = power set= Weierstrass p, U+2118 ISOamso
      "image"    : 8465, // blackletter capital I = imaginary part,U+2111 ISOamso
      "real"     : 8476, // blackletter capital R = real part symbol,U+211C ISOamso
      "trade"    : 8482, // trade mark sign, U+2122 ISOnum
      "alefsym"  : 8501, // alef symbol = first transfinite cardinal,U+2135 NEW

      // alef symbol is NOT the same as hebrew letter alef,U+05D0 although the same glyph could be used to depict both characters
      // Arrows
      "larr"     : 8592, // leftwards arrow, U+2190 ISOnum
      "uarr"     : 8593, // upwards arrow, U+2191 ISOnum-->
      "rarr"     : 8594, // rightwards arrow, U+2192 ISOnum
      "darr"     : 8595, // downwards arrow, U+2193 ISOnum
      "harr"     : 8596, // left right arrow, U+2194 ISOamsa
      "crarr"    : 8629, // downwards arrow with corner leftwards= carriage return, U+21B5 NEW
      "lArr"     : 8656, // leftwards double arrow, U+21D0 ISOtech

      // ISO 10646 does not say that lArr is the same as the 'is implied by' arrowbut also does not have any other character for that function. So ? lArr canbe used for 'is implied by' as ISOtech suggests
      "uArr"     : 8657, // upwards double arrow, U+21D1 ISOamsa
      "rArr"     : 8658, // rightwards double arrow,U+21D2 ISOtech

      // ISO 10646 does not say this is the 'implies' character but does not have another character with this function so ?rArr can be used for 'implies' as ISOtech suggests
      "dArr"     : 8659, // downwards double arrow, U+21D3 ISOamsa
      "hArr"     : 8660, // left right double arrow,U+21D4 ISOamsa

      // Mathematical Operators
      "forall"   : 8704, // for all, U+2200 ISOtech
      "part"     : 8706, // partial differential, U+2202 ISOtech
      "exist"    : 8707, // there exists, U+2203 ISOtech
      "empty"    : 8709, // empty set = null set = diameter,U+2205 ISOamso
      "nabla"    : 8711, // nabla = backward difference,U+2207 ISOtech
      "isin"     : 8712, // element of, U+2208 ISOtech
      "notin"    : 8713, // not an element of, U+2209 ISOtech
      "ni"       : 8715, // contains as member, U+220B ISOtech

      // should there be a more memorable name than 'ni'?
      "prod"     : 8719, // n-ary product = product sign,U+220F ISOamsb

      // prod is NOT the same character as U+03A0 'greek capital letter pi' though the same glyph might be used for both
      "sum"      : 8721, // n-ary summation, U+2211 ISOamsb

      // sum is NOT the same character as U+03A3 'greek capital letter sigma' though the same glyph might be used for both
      "minus"    : 8722, // minus sign, U+2212 ISOtech
      "lowast"   : 8727, // asterisk operator, U+2217 ISOtech
      "radic"    : 8730, // square root = radical sign,U+221A ISOtech
      "prop"     : 8733, // proportional to, U+221D ISOtech
      "infin"    : 8734, // infinity, U+221E ISOtech
      "ang"      : 8736, // angle, U+2220 ISOamso
      "and"      : 8743, // logical and = wedge, U+2227 ISOtech
      "or"       : 8744, // logical or = vee, U+2228 ISOtech
      "cap"      : 8745, // intersection = cap, U+2229 ISOtech
      "cup"      : 8746, // union = cup, U+222A ISOtech
      "int"      : 8747, // integral, U+222B ISOtech
      "there4"   : 8756, // therefore, U+2234 ISOtech
      "sim"      : 8764, // tilde operator = varies with = similar to,U+223C ISOtech

      // tilde operator is NOT the same character as the tilde, U+007E,although the same glyph might be used to represent both
      "cong"     : 8773, // approximately equal to, U+2245 ISOtech
      "asymp"    : 8776, // almost equal to = asymptotic to,U+2248 ISOamsr
      "ne"       : 8800, // not equal to, U+2260 ISOtech
      "equiv"    : 8801, // identical to, U+2261 ISOtech
      "le"       : 8804, // less-than or equal to, U+2264 ISOtech
      "ge"       : 8805, // greater-than or equal to,U+2265 ISOtech
      "sub"      : 8834, // subset of, U+2282 ISOtech
      "sup"      : 8835, // superset of, U+2283 ISOtech

      // note that nsup, 'not a superset of, U+2283' is not covered by the Symbol font encoding and is not included. Should it be, for symmetry?It is in ISOamsn  --> <!ENTITY nsub": 8836,  //not a subset of, U+2284 ISOamsn
      "sube"     : 8838, // subset of or equal to, U+2286 ISOtech
      "supe"     : 8839, // superset of or equal to,U+2287 ISOtech
      "oplus"    : 8853, // circled plus = direct sum,U+2295 ISOamsb
      "otimes"   : 8855, // circled times = vector product,U+2297 ISOamsb
      "perp"     : 8869, // up tack = orthogonal to = perpendicular,U+22A5 ISOtech
      "sdot"     : 8901, // dot operator, U+22C5 ISOamsb

      // dot operator is NOT the same character as U+00B7 middle dot
      // Miscellaneous Technical
      "lceil"    : 8968, // left ceiling = apl upstile,U+2308 ISOamsc
      "rceil"    : 8969, // right ceiling, U+2309 ISOamsc
      "lfloor"   : 8970, // left floor = apl downstile,U+230A ISOamsc
      "rfloor"   : 8971, // right floor, U+230B ISOamsc
      "lang"     : 9001, // left-pointing angle bracket = bra,U+2329 ISOtech

      // lang is NOT the same character as U+003C 'less than' or U+2039 'single left-pointing angle quotation mark'
      "rang"     : 9002, // right-pointing angle bracket = ket,U+232A ISOtech

      // rang is NOT the same character as U+003E 'greater than' or U+203A 'single right-pointing angle quotation mark'
      // Geometric Shapes
      "loz"      : 9674, // lozenge, U+25CA ISOpub

      // Miscellaneous Symbols
      "spades"   : 9824, // black spade suit, U+2660 ISOpub

      // black here seems to mean filled as opposed to hollow
      "clubs"    : 9827, // black club suit = shamrock,U+2663 ISOpub
      "hearts"   : 9829, // black heart suit = valentine,U+2665 ISOpub
      "diams"    : 9830, // black diamond suit, U+2666 ISOpub

      // Latin Extended-A
      "OElig"    : 338, //  -- latin capital ligature OE,U+0152 ISOlat2
      "oelig"    : 339, //  -- latin small ligature oe, U+0153 ISOlat2

      // ligature is a misnomer, this is a separate character in some languages
      "Scaron"   : 352, //  -- latin capital letter S with caron,U+0160 ISOlat2
      "scaron"   : 353, //  -- latin small letter s with caron,U+0161 ISOlat2
      "Yuml"     : 376, //  -- latin capital letter Y with diaeresis,U+0178 ISOlat2

      // Spacing Modifier Letters
      "circ"     : 710, //  -- modifier letter circumflex accent,U+02C6 ISOpub
      "tilde"    : 732, // small tilde, U+02DC ISOdia

      // General Punctuation
      "ensp"     : 8194, // en space, U+2002 ISOpub
      "emsp"     : 8195, // em space, U+2003 ISOpub
      "thinsp"   : 8201, // thin space, U+2009 ISOpub
      "zwnj"     : 8204, // zero width non-joiner,U+200C NEW RFC 2070
      "zwj"      : 8205, // zero width joiner, U+200D NEW RFC 2070
      "lrm"      : 8206, // left-to-right mark, U+200E NEW RFC 2070
      "rlm"      : 8207, // right-to-left mark, U+200F NEW RFC 2070
      "ndash"    : 8211, // en dash, U+2013 ISOpub
      "mdash"    : 8212, // em dash, U+2014 ISOpub
      "lsquo"    : 8216, // left single quotation mark,U+2018 ISOnum
      "rsquo"    : 8217, // right single quotation mark,U+2019 ISOnum
      "sbquo"    : 8218, // single low-9 quotation mark, U+201A NEW
      "ldquo"    : 8220, // left double quotation mark,U+201C ISOnum
      "rdquo"    : 8221, // right double quotation mark,U+201D ISOnum
      "bdquo"    : 8222, // double low-9 quotation mark, U+201E NEW
      "dagger"   : 8224, // dagger, U+2020 ISOpub
      "Dagger"   : 8225, // double dagger, U+2021 ISOpub
      "permil"   : 8240, // per mille sign, U+2030 ISOtech
      "lsaquo"   : 8249, // single left-pointing angle quotation mark,U+2039 ISO proposed
      // lsaquo is proposed but not yet ISO standardized
      "rsaquo"   : 8250, // single right-pointing angle quotation mark,U+203A ISO proposed
      // rsaquo is proposed but not yet ISO standardized
      "euro"     : 8364 //  -- euro sign, U+20AC NEW
    }
  }

} );
