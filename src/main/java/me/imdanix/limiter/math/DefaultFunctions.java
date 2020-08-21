package me.imdanix.limiter.math;

/**
 * Some default math functions
 */
enum DefaultFunctions implements MathBase.Function {
    ABS {
        @Override
        public double eval(double a, double... num) {
            return Math.abs(a);
        }
    },
    CBRT {
        @Override
        public double eval(double a, double... num) {
            return Math.cbrt(a);
        }
    },
    CEIL {
        @Override
        public double eval(double a, double... num) {
            return Math.ceil(a);
        }
    },
    EXP {
        @Override
        public double eval(double a, double... num) {
            return Math.exp(a);
        }
    },
    EXPM1 {
        @Override
        public double eval(double a, double... num) {
            return Math.expm1(a);
        }
    },
    FLOOR {
        @Override
        public double eval(double a, double... num) {
            return Math.floor(a);
        }
    },
    GETEXPONENT {
        @Override
        public double eval(double a, double... num) {
            return Math.getExponent(a);
        }
    },
    LOG {
        @Override
        public double eval(double a, double... num) {
            return Math.log(a);
        }
    },
    LOG10 {
        @Override
        public double eval(double a, double... num) {
            return Math.log10(a);
        }
    },
    LOG1P {
        @Override
        public double eval(double a, double... num) {
            return Math.log1p(a);
        }
    },
    MAX {
        @Override
        public double eval(double a, double... num) {
            if(num.length > 0) {
                double max = a;
                for(double j : num)
                    max = Math.max(max, j);
                return max;
            }
            return a;
        }
    },
    MIN {
        @Override
        public double eval(double a, double... num) {
            if(num.length > 0) {
                double min = a;
                for(double j : num)
                    min = Math.min(min, j);
                return min;
            }
            return a;
        }
    },
    NEXTAFTER {
        @Override
        public double eval(double a, double... num) {
            return num.length > 0 ? Math.nextAfter(a, num[0]) : a;
        }
    },
    NEXTDOWN {
        @Override
        public double eval(double a, double... num) {
            return Math.nextDown(a);
        }
    },
    NEXTUP {
        @Override
        public double eval(double a, double... num) {
            return Math.nextUp(a);
        }
    },
    ROUND {
        @Override
        public double eval(double a, double... num) {
            return Math.round(a);
        }
    },
    RINT {
        @Override
        public double eval(double a, double... num) {
            return Math.rint(a);
        }
    },
    SIGNUM {
        @Override
        public double eval(double a, double... num) {
            return Math.signum(a);
        }
    },
    SQRT {
        @Override
        public double eval(double a, double... num) {
            return Math.sqrt(a);
        }
    },
    IEEEREMAINDER {
        @Override
        public double eval(double a, double... num) {
            return num.length > 0 ? Math.IEEEremainder(a, num[0]) : a;
        }
    },
    FORMATFLOAT {
        @Override
        public double eval(double a, double... num) {
            // TODO: Count of chars after dot
            return Math.round(a * 100) / 100;
        }
    }
}
