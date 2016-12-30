package com.yalin.style.util;

/**
 * YaLin 2016/12/30.
 */

public class MathUtil {

  public static float constrain(float min, float max, float v) {
    return Math.max(min, Math.min(max, v));
  }

  public static float interpolate(float x1, float x2, float f) {
    return x1 + (x2 - x1) * f;
  }

  public static float uninterpolate(float x1, float x2, float v) {
    if (x2 - x1 == 0) {
      throw new IllegalArgumentException("Can't reverse interpolate with domain size of 0");
    }
    return (v - x1) / (x2 - x1);
  }

  public static int floorEven(int num) {
    return num & ~0x01;
  }

  public static int roundMult4(int num) {
    return (num + 2) & ~0x03;
  }

  // divide two integers but round up
  // see http://stackoverflow.com/a/7446742/102703
  public static int intDivideRoundUp(int num, int divisor) {
    int sign = (num > 0 ? 1 : -1) * (divisor > 0 ? 1 : -1);
    return sign * (Math.abs(num) + Math.abs(divisor) - 1) / Math.abs(divisor);
  }

}
