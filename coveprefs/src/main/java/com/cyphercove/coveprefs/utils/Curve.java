/*
 * Copyright (C) 2017 Cypher Cove, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cyphercove.coveprefs.utils;

/**
 * Various animation curves. Each method returns a modified fraction out.
 * <p>
 * Equations borrowed from
 * <a href="https://github.com/libgdx/libgdx/blob/master/gdx/src/com/badlogic/gdx/math/Interpolation.java">
 *     LibGDX's Interpolation class.</a>
 */
@SuppressWarnings("WeakerAccess")
public class Curve {

    public static float smoothStep (float fraction) {
        return fraction * fraction * (3 - 2 * fraction);
    }

    public static float slowInSlowOut (float fraction) {
        return slowInSlowOut(fraction, 3);
    }

    public static float slowInSlowOut (float fraction, float power){
        if (fraction <= 0.5f) return (float)Math.pow(fraction * 2, 2) / 2;
        return (float)Math.pow((fraction - 1) * 2, power) / (power % 2 == 0 ? -2 : 2) + 1;
    }

    public static float slowInFastOut (float fraction){
        return slowInFastOut(fraction, 3);
    }

    public static float slowInFastOut (float fraction, float power){
        return (float)Math.pow(fraction, power);
    }

    public static float fastInSlowOut (float fraction){
        return fastInSlowOut(fraction, 3);
    }

    public static float fastInSlowOut (float fraction, float power){
        return (float)Math.pow(fraction - 1, power) * (power % 2 == 0 ? -1 : 1) + 1;
    }
}
