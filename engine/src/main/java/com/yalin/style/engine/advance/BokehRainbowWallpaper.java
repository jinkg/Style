/*
 * Copyright (C) 2007 Google Inc.
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
package com.yalin.style.engine.advance;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.SurfaceHolder;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class BokehRainbowWallpaper extends AnimationWallpaper {

	public BokehRainbowWallpaper(Context host) {
		super(host);
	}

	@Override
	public Engine onCreateEngine() {
		return new BokehEngine();
	}

	class BokehEngine extends AnimationEngine {
		int offsetX;
		int offsetY;
		int height;
		int width;
		int visibleWidth;

		Set<BokehRainbowCircle> circles = new HashSet<BokehRainbowCircle>();

		int iterationCount = 0;

		Paint paint = new Paint();

		@Override
		public void onCreate(SurfaceHolder surfaceHolder) {
			super.onCreate(surfaceHolder);

			// By default we don't get touch events, so enable them.
			setTouchEventsEnabled(true);
		}

		@Override
		public void onSurfaceChanged(SurfaceHolder holder, int format,
				int width, int height) {

			this.height = height;
			if (this.isPreview()) {
				this.width = width;
			} else {
				this.width = 2 * width;
			}
			this.visibleWidth = width;

			for (int i = 0; i < 20; i++) {
				this.createRandomCircle();
			}

			super.onSurfaceChanged(holder, format, width, height);
		}

		@Override
		public void onOffsetsChanged(float xOffset, float yOffset,
				float xOffsetStep, float yOffsetStep, int xPixelOffset,
				int yPixelOffset) {
			// store the offsets
			this.offsetX = xPixelOffset;
			this.offsetY = yPixelOffset;

			super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep,
					xPixelOffset, yPixelOffset);
		}

		@Override
		public Bundle onCommand(String action, int x, int y, int z,
				Bundle extras, boolean resultRequested) {
			if ("android.wallpaper.tap".equals(action)) {
				createCircle(x - this.offsetX, y - this.offsetY);
			}
			return super.onCommand(action, x, y, z, extras, resultRequested);
		}

		@Override
		protected void drawFrame() {
			SurfaceHolder holder = getSurfaceHolder();

			Canvas c = null;
			try {
				c = holder.lockCanvas();
				if (c != null) {
					draw(c);
				}
			} finally {
				if (c != null)
					holder.unlockCanvasAndPost(c);
			}
		}

		void draw(Canvas c) {
			c.save();
			c.drawColor(0xff000000);

			synchronized (circles) {
				for (BokehRainbowCircle circle : circles) {
					if (circle.alpha == 0)
						continue;

					// intersects with the screen?
					float minX = circle.x - circle.radius;
					if (minX > (-this.offsetX + this.visibleWidth)) {
						continue;
					}
					float maxX = circle.x + circle.radius;
					if (maxX < -this.offsetX) {
						continue;
					}

					paint.setAntiAlias(true);

					// paint the fill
					paint.setColor(Color.argb(circle.alpha, Color
							.red(circle.color), Color.green(circle.color),
							Color.blue(circle.color)));
					paint.setStyle(Paint.Style.FILL_AND_STROKE);
					c.drawCircle(circle.x + this.offsetX, circle.y
							+ this.offsetY, circle.radius, paint);

					// paint the contour
					paint.setColor(Color.argb(circle.alpha, 63 + 3 * Color
							.red(circle.color) / 4, 63 + 3 * Color
							.green(circle.color) / 4, 63 + 3 * Color
							.blue(circle.color) / 4));
					paint.setStyle(Paint.Style.STROKE);
					paint.setStrokeWidth(3.0f);
					c.drawCircle(circle.x + this.offsetX, circle.y
							+ this.offsetY, circle.radius, paint);
				}
			}

			c.restore();
		}

		@Override
		protected void iteration() {
			synchronized (circles) {
				for (Iterator<BokehRainbowCircle> it = circles.iterator(); it
						.hasNext();) {
					BokehRainbowCircle circle = it.next();
					circle.tick();
					if (circle.isDone())
						it.remove();
				}
				iterationCount++;
				if (isPreview() || iterationCount % 2 == 0)
					createRandomCircle();
			}

			super.iteration();
		}

		void createRandomCircle() {
			int x = (int) (width * Math.random());
			int y = (int) (height * Math.random());
			createCircle(x, y);
		}

		int getColor(float yFraction) {
			return Color.HSVToColor(new float[] { 360.0f * yFraction, 1.0f,
					1.0f });
		}

		void createCircle(int x, int y) {
			float radius = (float) (40 + 20 * Math.random());

			float yFraction = (float) y / (float) height;
			yFraction = yFraction + 0.05f - (float) (0.1f * (Math.random()));
			if (yFraction < 0.0f)
				yFraction += 1.0f;
			if (yFraction > 1.0f)
				yFraction -= 1.0f;
			int color = getColor(yFraction);

			int steps = 40 + (int) (20 * Math.random());
			BokehRainbowCircle circle = new BokehRainbowCircle(x, y, radius,
					color, steps);
			synchronized (this.circles) {
				this.circles.add(circle);
			}
		}

	}

}
