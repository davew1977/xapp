/*
 *
 * Date: 2011-feb-24
 * Author: davidw
 *
 */
package net.sf.xapp.uifwk.anim;

public enum Effect
{
    NONE
            {
                @Override
                double transform(double workDone)
                {
                    return workDone;
                }},
    BOTH
            {
                @Override
                double transform(double workDone)
                {
                    return Math.pow(Math.sin(workDone * 0.5 * Math.PI), 2.0);
                }},
    ACCELERATE
            {
                @Override
                double transform(double workDone)
                {
                    return 1.0 - Math.sqrt(1.0 - workDone * workDone);
                }},
    DECELERATE
            {
                @Override
                double transform(double workDone)
                {
                    return Math.sqrt(1.0 - (workDone - 1.0) * (workDone - 1.0));
                }},
    INVERSE
            {
                @Override
                double transform(double workDone)
                {
                    return (2.0 * Math.asin(Math.sqrt(workDone))) / Math.PI;
                }};

    abstract double transform(double workDone);
}
