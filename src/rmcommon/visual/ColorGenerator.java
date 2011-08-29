/**
 * Created on Aug 29, 2011 in Project JRMCommons
 * Location: rmcommon.visual.ColorGenerator.java
 */
package rmcommon.visual;

import rmcommon.geometry.DiscretizationType;

/**
 * @author Daniel Wirtz
 * @date Aug 29, 2011
 *
 */
public class ColorGenerator {
	
	/**
	 * The color map to use
	 */
	public ColorMap ColorMap;
	
	/**
	 * default alpha value, use 1.0f for nonblend rendering
	 */
	public float alphaValue = 0.8f;
	
	/**
	 * Computes a 4-tuple color array with values R, G, B, Alpha for the given field values.
	 * @param fieldValues
	 * @return The color array for the given fieldValues and color map
	 */
	public float[] computeColors(float[] fieldValues) {
				
		float[] colors = new float[fieldValues.length * 4];
		float min = getMin(fieldValues);
		float max = getMax(fieldValues);
		if (Math.abs(min - max) < 1e-8) {
			for (int i = 0; i < fieldValues.length; i++) {
				colors[i * 4 + 0] = 0.0f;
				colors[i * 4 + 1] = 0.0f;
				colors[i * 4 + 2] = 1.0f;
				colors[i * 4 + 3] = alphaValue;
			}
		} else {
			// calculate color data
			for (int i = 0; i < fieldValues.length; i++) {
				float tmpvar = (fieldValues[i] - min)
						/ (max - min);
				if (tmpvar <= 0.125f) {
					colors[i * 4 + 0] = 0.0f;
					colors[i * 4 + 1] = 0.0f;
					colors[i * 4 + 2] = 0.5f + tmpvar / 0.25f;
					colors[i * 4 + 3] = alphaValue;
				}
				if ((tmpvar > 0.125f) && (tmpvar <= 0.375f)) {
					colors[i * 4 + 0] = 0.0f;
					colors[i * 4 + 1] = 0.0f + (tmpvar - 0.125f) / 0.25f;
					colors[i * 4 + 2] = 1.0f;
					colors[i * 4 + 3] = alphaValue;
				}
				if ((tmpvar > 0.375f) && (tmpvar <= 0.625f)) {
					colors[i * 4 + 0] = 0.0f + (tmpvar - 0.375f) / 0.25f;
					colors[i * 4 + 1] = 1.0f;
					colors[i * 4 + 2] = 1.0f - (tmpvar - 0.375f) / 0.25f;
					colors[i * 4 + 3] = alphaValue;
				}
				if ((tmpvar > 0.625f) && (tmpvar <= 0.875f)) {
					colors[i * 4 + 0] = 1.0f;
					colors[i * 4 + 1] = 1.0f - (tmpvar - 0.625f) / 0.25f;
					colors[i * 4 + 2] = 0.0f;
					colors[i * 4 + 3] = alphaValue;
				}
				if (tmpvar > 0.875f) {
					colors[i * 4 + 0] = 1.0f - (tmpvar - 0.875f) / 0.25f;
					colors[i * 4 + 1] = 0.0f;
					colors[i * 4 + 2] = 0.0f;
					colors[i * 4 + 3] = alphaValue;
				}
			}
		}
		return colors;
	}
	
	private float getMin(float[] values) {
		float min = values[0];
		for (int j = 0; j < values.length; j++) {
			min = (min > values[j]) ? values[j] : min;
		}
		return min;
	}
	
	private float getMax(float[] values) {
		float min = values[0];
		for (int j = 0; j < values.length; j++) {
			min = (min < values[j]) ? values[j] : min;
		}
		return min;
	}


}
