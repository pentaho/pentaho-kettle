#ifndef ECLIPSE_UNICODE_H
#define ECLIPSE_UNICODE_H

#ifdef _WIN32

#ifdef UNICODE
#define _UNICODE
#endif
#include <windows.h>
#include <tchar.h>
#include <ctype.h>

#ifdef __MINGW32__
# ifdef UNICODE
#  ifndef _TCHAR
#   define _TCHAR TCHAR
#  endif /* _TCHAR */
#  ifndef _tgetcwd
#   define _tgetcwd _wgetcwd
#  endif /* _tgetcwd */
#  ifndef _tstat
#   define _tstat _wstat
#  endif /* _tstat */
# else /* UNICODE */
#  ifndef _TCHAR
#   define _TCHAR char
#  endif /* _TCHAR */
#  ifndef _tgetcwd
#   define _tgetcwd getcwd
#  endif /* _tgetcwd */
#  ifndef _tstat
#   define _tstat _stat
#  endif /* _tstat */
# endif /* UNICODE */
#endif /* __MINGW32__ */

#define _T_ECLIPSE _T

#else /* Platforms other than Windows */

#define _TCHAR char
#define _T_ECLIPSE(s) s
#define _fgetts fgets
#define _stat stat
#define _stprintf sprintf
#define _stscanf sscanf
#define _tcscat strcat
#define _tcschr strchr
#define _tcscmp strcmp
#define _tcscpy strcpy
#define _tcsdup strdup
#define _tcsicmp strcasecmp
#define _tcslen strlen
#define _tcsncpy strncpy
#define _tcsrchr strrchr
#define _tfopen fopen
#define _tgetcwd getcwd
#define _tgetenv getenv
#ifndef LINUX
#define _totupper toupper
#endif /* LINUX */
#define _tprintf printf
#define _tstat stat

#endif /* _WIN32 */

#endif /* ECLIPSE_UNICODE_H */
