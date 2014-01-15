#ifndef TEMPLATES_H_
#define TEMPLATES_H_

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <math.h>
#include <stdint.h>
#include <assert.h>

#include <sys/time.h>

#include <immintrin.h>

#include <ctype.h>
#include <string>

#define MROWS  500
#define MCOLS  1000

#define CAT(X,Y) X####Y
#define GEN_INTRINSIC(X,Y) CAT(X,Y)

#define ALIGNED __attribute__((aligned(32)))

typedef union __attribute__((aligned(32))) {
        ALIGNED __m256 ALIGNED d;
        ALIGNED __m128i ALIGNED s[2];
        ALIGNED float  ALIGNED f[8];
        ALIGNED __m256i ALIGNED i;
} ALIGNED mix_F ALIGNED;

typedef union ALIGNED {
  __m128i vec ;
  __m128 vecf ;
  uint32_t masks[4] ;
} MaskVec_F ;

typedef union ALIGNED
{
        ALIGNED __m128i ALIGNED i;
        ALIGNED __m128 ALIGNED f;
} ALIGNED IF_128f ALIGNED;

typedef union ALIGNED
{
        ALIGNED int    ALIGNED i;
        ALIGNED float  ALIGNED f;
} ALIGNED IF_32 ALIGNED;

typedef union __attribute__((aligned(32))) {
        ALIGNED __m256d ALIGNED d;
        ALIGNED __m128i ALIGNED s[2];
        ALIGNED double  ALIGNED f[4];
        ALIGNED __m256i ALIGNED i;
} ALIGNED mix_D ALIGNED;

typedef union ALIGNED {
  __m128i vec ;
  __m128d vecf ;
  uint64_t masks[2] ;
} MaskVec_D ;

typedef union ALIGNED
{
        ALIGNED __m128i ALIGNED i;
        ALIGNED __m128d ALIGNED f;
} ALIGNED IF_128d ALIGNED;

typedef union ALIGNED
{
        ALIGNED int64_t ALIGNED i;
        ALIGNED double  ALIGNED f;
} ALIGNED IF_64 ALIGNED;

template<class T>
struct Context{};

template<>
struct Context<double>
{
        Context()
        {
                for (int x = 0; x < 128; x++)
                        ph2pr[x] = pow(10.0, -((double)x) / 10.0);

                INITIAL_CONSTANT = ldexp(1.0, 1020.0);
                LOG10_INITIAL_CONSTANT = log10(INITIAL_CONSTANT);
                RESULT_THRESHOLD = 0.0;
        }

        double LOG10(double v){ return log10(v); }

        static double _(double n){ return n; }
        static double _(float n){ return ((double) n); }
        double ph2pr[128];
        double INITIAL_CONSTANT;
        double LOG10_INITIAL_CONSTANT;
        double RESULT_THRESHOLD;
};

template<>
struct Context<float>
{
        Context()
        {
                for (int x = 0; x < 128; x++)
                {
                        ph2pr[x] = powf(10.f, -((float)x) / 10.f);
                }

                INITIAL_CONSTANT = ldexpf(1.f, 120.f);
                LOG10_INITIAL_CONSTANT = log10f(INITIAL_CONSTANT);
                RESULT_THRESHOLD = ldexpf(1.f, -110.f);
        }

        float LOG10(float v){ return log10f(v); }

        static float _(double n){ return ((float) n); }
        static float _(float n){ return n; }
        float ph2pr[128];
        float INITIAL_CONSTANT;
        float LOG10_INITIAL_CONSTANT;
        float RESULT_THRESHOLD;
};


typedef struct
{
        int rslen, haplen, *q, *i, *d, *c;
        char *hap, *rs;
        int *ihap;
        int *irs;
} testcase;


template<class T>
std::string to_string(T obj)
{
  stringstream ss;
  string ret_string;
  ss.clear();
  ss << std::scientific << obj;
  ss >> ret_string;
  ss.clear();
  return ret_string;
}
void debug_dump(std::string filename, std::string s, bool to_append, bool add_newline=true);

int normalize(char c);


int read_testcase(testcase *tc)
{
        char *q, *i, *d, *c, *line = NULL;
        int _q, _i, _d, _c;
        int x, size = 0;
        ssize_t read;

        read = getline(&line, (size_t *) &size, stdin);
        if (read == -1)
                return -1;

        tc->hap = (char *) malloc(size);
        tc->rs = (char *) malloc(size);
        q = (char *) malloc(size);
        i = (char *) malloc(size);
        d = (char *) malloc(size);
        c = (char *) malloc(size);
        tc->ihap = (int *) malloc(size*sizeof(int));
        tc->irs = (int *) malloc(size*sizeof(int));


        if (sscanf(line, "%s %s %s %s %s %s\n", tc->hap, tc->rs, q, i, d, c) != 6)
                return -1;

        tc->haplen = strlen(tc->hap);
        tc->rslen = strlen(tc->rs);
        tc->q = (int *) malloc(sizeof(int) * tc->rslen);
        tc->i = (int *) malloc(sizeof(int) * tc->rslen);
        tc->d = (int *) malloc(sizeof(int) * tc->rslen);
        tc->c = (int *) malloc(sizeof(int) * tc->rslen);

        tc->ihap = (int *) malloc(tc->haplen*sizeof(int));
        tc->irs = (int *) malloc(tc->rslen*sizeof(int));

        for (x = 0; x < tc->rslen; x++)
        {
                _q = normalize(q[x]);
                _i = normalize(i[x]);
                _d = normalize(d[x]);
                _c = normalize(c[x]);
                tc->q[x] = (_q < 6) ? 6 : _q;
                tc->i[x] = _i;
                tc->d[x] = _d;
                tc->c[x] = _c;
                tc->irs[x] = tc->rs[x];
        }

        for (x = 0; x < tc->haplen; x++)
                tc->ihap[x] = tc->hap[x];

        free(q);
        free(i);
        free(d);
        free(c);
        free(line);

        return 0;
}

#define NUM_DISTINCT_CHARS 5
#define AMBIG_CHAR 4

class ConvertChar {

  static uint8_t conversionTable[255] ;

public:

  static void init() {
    assert (NUM_DISTINCT_CHARS == 5) ;
    assert (AMBIG_CHAR == 4) ;

    conversionTable['A'] = 0 ;
    conversionTable['C'] = 1 ;
    conversionTable['T'] = 2 ;
    conversionTable['G'] = 3 ;
    conversionTable['N'] = 4 ;
  }

  static inline uint8_t get(uint8_t input) {
    return conversionTable[input] ;
  }

} ;


#endif


