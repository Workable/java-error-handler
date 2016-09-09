package com.workable.errorhandler.matchers.retrofit;

import org.junit.Assert;
import org.junit.Test;

public class RangeTest {

    @Test
    public void test_check_down_bound() {
        Range range = Range.of(400, 500);

        Assert.assertTrue(range.contains(400));
        Assert.assertFalse(range.contains(399));
    }

    @Test
    public void test_check_upper_bound() {
        Range range = Range.of(400, 500);

        Assert.assertTrue(range.contains(500));
        Assert.assertFalse(range.contains(501));
    }

    @Test
    public void test_is_in_range() {
        Range range = Range.of(400, 500);

        Assert.assertTrue(range.contains(450));
    }

    @Test
    public void test_same_range() {
        Range range = Range.of(400, 400);

        Assert.assertTrue(range.contains(400));
        Assert.assertFalse(range.contains(500));
    }

    @Test
    public void test_range_equality() {
        Range range = Range.of(400, 500);


        Assert.assertTrue(range.equals(Range.of(400, 500)));
        Assert.assertFalse(range.equals(Range.of(401, 500)));

    }

}
