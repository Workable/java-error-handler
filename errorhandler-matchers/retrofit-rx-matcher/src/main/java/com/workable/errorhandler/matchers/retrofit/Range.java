package com.workable.errorhandler.matchers.retrofit;

/**
 * Range class for HTTP status codes
 */
public class Range {

    private int lowerBound;
    private int upperBound;

    /**
     * Creates a Range object with lower and upper bound
     * @param lowerBound lower limit of Range
     * @param upperBound upper limit of Range
     *
     * @return a Range instance
     */
    public static Range of(int lowerBound, int upperBound) {
        return new Range(lowerBound, upperBound);
    }

    private Range(int lowerBound, int upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    /**
     * Checks if the passed httpStatusCode is contained in given range
     *
     * @param httpStatusCode the status code to check
     * @return true if contains, otherwise false
     */
    public boolean contains(int httpStatusCode) {
        return httpStatusCode >= lowerBound && httpStatusCode <= upperBound;
    }

    public int getLowerBound() {
        return lowerBound;
    }

    public int getUpperBound() {
        return upperBound;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Range range = (Range) o;

        if (lowerBound != range.lowerBound) return false;
        return upperBound == range.upperBound;

    }

    @Override
    public int hashCode() {
        int result = lowerBound;
        result = 31 * result + upperBound;
        return result;
    }
}
