/*
 * Copyright 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yalin.style.view.component

import android.content.Context
import android.content.res.TypedArray
import android.support.v7.widget.AppCompatTextView
import android.util.AttributeSet

import com.yalin.style.R


class ShadowDipsTextView : AppCompatTextView {
    constructor(context: Context) : super(context) {
        init(context, null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) :
            super(context, attrs, defStyle) {
        init(context, attrs, defStyle)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyle: Int) {
        val a = context.obtainStyledAttributes(attrs,
                R.styleable.ShadowDipsTextView, defStyle, 0)
        val shadowDx = a.getDimensionPixelSize(R.styleable.ShadowDipsTextView_shadowDx, 0)
        val shadowDy = a.getDimensionPixelSize(R.styleable.ShadowDipsTextView_shadowDy, 0)
        val shadowRadius = a.getDimensionPixelSize(R.styleable.ShadowDipsTextView_shadowRadius, 0)
        val shadowColor = a.getColor(R.styleable.ShadowDipsTextView_shadowColor, 0)
        if (shadowColor != 0) {
            setShadowLayer(shadowRadius.toFloat(),
                    shadowDx.toFloat(), shadowDy.toFloat(), shadowColor)
        }
        a.recycle()
    }
}

