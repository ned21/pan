# get negative index greater than size
#
# @expect=org.quattor.pan.exceptions.EvaluationException
#
object template list8;

"/a" = {
    t = list(1 ,2);
    t[-3];
};
