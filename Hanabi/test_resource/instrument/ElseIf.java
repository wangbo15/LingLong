package cn.edu.pku.sei.plde.hanabi.test;

public class ElseIf{
    public int getOffsetFromLocal(long instantLocal, int offsetLocal) {
        final long instantAdjusted = instantLocal - offsetLocal;
        final int offsetAdjusted = 2;
        if (offsetLocal != offsetAdjusted) {
            if ((offsetLocal - offsetAdjusted) < 0) {
                long nextLocal = 3;
                long nextAdjusted = 4;
                if (nextLocal != nextAdjusted) {
                    return offsetLocal;
                }
            }
        } else if (offsetLocal > 0) {
            long prev = 5;
            if (prev < instantAdjusted) {
                int offsetPrev = 6;
                int diff = offsetPrev - offsetLocal;
                if (instantAdjusted - prev <= diff) {
                    return offsetPrev;
                }
            }
        }
        return offsetAdjusted;
    }
}