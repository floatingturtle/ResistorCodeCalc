/**
 * 
 * Implements a Resistor View.
 * 
 * @author Jason Warren
 * 
 */

package edu.cuny.citytech.daedalus.widgets;

import java.util.ArrayList;
import edu.cuny.citytech.daedalus.labyrinth.R;
import edu.cuny.citytech.daedalus.utils.UnitConversion;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

public class ResistorView extends View {

	final float SCALE;

	// Background image constants.
	final int BG_WIDTH = 256; // The width of the background image in actual
								// pixels
	final int BG_HEIGHT = 80; // The height of the bg image in actual pixels.
	final int MSB_LEFT = 72;
	final int MSB_TOP = 18;
	final int MSB_RIGHT = 87;
	final int MSB_BOTTOM = 62;
	final int LSB_LEFT = 105;
	final int LSB_TOP = 21;
	final int LSB_RIGHT = 119;
	final int LSB_BOTTOM = 59;
	final int MULT_LEFT = 135;
	final int MULT_TOP = 21;
	final int MULT_RIGHT = 151;
	final int MULT_BOTTOM = 59;
	final int TOL_LEFT = 169;
	final int TOL_TOP = 17;
	final int TOL_RIGHT = 183;
	final int TOL_BOTTOM = 62;

	/**
	 * Interface to let clients know when the value of this resistor's color bands has changed.
	 *
	 */
	public interface OnValueChangedListener {

		public void onValueChanged();
	}

	ArrayList<OnValueChangedListener> onValueChangedListeners;

	public enum ResistorColor {
		SILVER(-2), GOLD(-1), BLACK(0), BROWN(1), RED(2), ORANGE(3), YELLOW(4), GREEN(5),
		BLUE(6), VIOLET(7), GRAY(8), WHITE(9);
		
		private int value;
		
		ResistorColor(int v){
			value = v;
		}
		
		public int getValue(){
			return value;
		}
		
		public static ResistorColor fromValue(int v){
			ResistorColor color;
			
			if(v == -2){
				color = ResistorColor.SILVER;
			}
			
			else if(v == -1){
				color = ResistorColor.GOLD;
			}
			
			else{
				color = ResistorColor.values()[v+2];
			}
			
			return color;
		}
		
		public int getColor() {

			int color;
			
			switch (this) {
			case BLACK:
				color = Color.BLACK;
				break;
			case BROWN:
				color = Color.rgb(139, 69, 19);
				break;
			case RED:
				color = Color.RED;
				break;
			case ORANGE:
				color = Color.rgb(255, 165, 0);
				break;
			case YELLOW:
				color = Color.rgb(255, 255, 0);
				break;
			case GREEN:
				color = Color.GREEN;
				break;
			case BLUE:
				color = Color.BLUE;
				break;
			case VIOLET:
				color = Color.rgb(148, 0, 211);
				break;
			case GRAY:
				color = Color.GRAY;
				break;
			case WHITE:
				color = Color.WHITE;
				break;
			case GOLD:
				color = Color.rgb(255, 215, 0);
				break;
			case SILVER:
				color = Color.rgb(192, 192, 192);
				break;
			default:
				color = Color.TRANSPARENT;
			}
			return color;
		}
	}

	Paint paint;
	ResistorColor msb;
	ResistorColor lsb;
	ResistorColor multiplier;
	ResistorColor tolerance;
	RectF msbBounds; // The bounds of the MSB band in absolute pixels
	RectF lsbBounds; // The bounds of the LSB band in absolute pixels
	RectF multiplierBounds; // The bounds of the Multiplier band in absolute
							// pixels.
	RectF toleranceBounds; // The bounds of the Tolerance band in absolute
							// pixels.

	/**
	 * The following description is taken from the superclass documentation:
	 * 
	 * Simple constructor to use when creating a view from code.
	 * 
	 * @param context	Context the view is running in, through which it can access the
	 * 					current theme, resources, etc.
	 */
	public ResistorView(Context context) {
		super(context);
		SCALE = getContext().getResources().getDisplayMetrics().density;
		initialize();
	}

	/**
	 * The following description is taken from the superclass documentation:
	 * 
	 * Perform inflation from XML and apply a class-specific base style.
	 * This constructor of View allows subclasses to use their own base style when they
	 * are inflating. For example, a Button class's constructor would call this version of
	 * the super class constructor and supply R.attr.buttonStyle for defStyle; this allows the
	 * theme's button style to modify all of the base view attributes (in particular its
	 * background) as well as the Button class's attributes.
	 * @param context	The Context the view is running in, through which it can access the
	 * current theme, resources, etc.
	 * @param attrs		The attributes of the XML tag that is inflating the view.
	 * @param defStyleAttr	An attribute in the current theme that contains a reference to
	 * a style resource to apply to this view. If 0, no default style will be applied.
	 */
	public ResistorView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		SCALE = getContext().getResources().getDisplayMetrics().density;
		initialize();
	}

	/**
	 * The following description is taken from the superclass documentation:
	 * 
	 * Constructor that is called when inflating a view from XML. This is called when a view 
	 * is being constructed from an XML file, supplying attributes that were specified in the 
	 * XML file. This version uses a default style of 0, so the only attribute values applied 
	 * are those in the Context's Theme and the given AttributeSet.The method onFinishInflate() 
	 * will be called after all children have been added.
	 * @param context	The Context the view is running in, through which it can access the 
	 * current theme, resources, etc.
	 * @param attrs		The attributes of the XML tag that is inflating the view.
	 */
	public ResistorView(Context context, AttributeSet attrs) {
		super(context, attrs);
		SCALE = getContext().getResources().getDisplayMetrics().density;
		initialize();
	}

	/**
	 * Called by constructors to initialize data members.
	 */
	private void initialize() {
		onValueChangedListeners = new ArrayList<OnValueChangedListener>();
		paint = new Paint();
		msb = ResistorColor.BLACK;
		lsb = ResistorColor.BLACK;
		multiplier = ResistorColor.BLACK;
		tolerance = ResistorColor.GOLD;

	}

	@Override
	protected Parcelable onSaveInstanceState() {

		Bundle bundle = new Bundle();
		bundle.putParcelable("instanceState", super.onSaveInstanceState());
		bundle.putSerializable("msb", msb);
		bundle.putSerializable("lsb", lsb);
		bundle.putSerializable("multiplier", multiplier);
		bundle.putSerializable("tolerance", tolerance);

		return bundle;
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {

		if (state instanceof Bundle) {
			Bundle bundle = (Bundle) state;
			msb = (ResistorColor) bundle.getSerializable("msb");
			lsb = (ResistorColor) bundle.getSerializable("lsb");
			multiplier = (ResistorColor) bundle.getSerializable("multiplier");
			tolerance = (ResistorColor) bundle.getSerializable("tolerance");
			
			state = bundle.getParcelable("instanceState");
		}

		super.onRestoreInstanceState(state);
	}

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		super.setClickable(true);
		super.setLongClickable(true);
		super.setHapticFeedbackEnabled(true);
		super.setBackgroundResource(R.drawable.resistor_blank_moderate_crop);
		Drawable d = super.getBackground();
		Rect b = new Rect(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
		int left = Math.round(((float) MSB_LEFT / BG_WIDTH) * b.right);
		int top = Math.round(((float) MSB_TOP / BG_HEIGHT) * b.bottom);
		int right = Math.round(((float) MSB_RIGHT / BG_WIDTH) * b.right);
		int bottom = Math.round(((float) MSB_BOTTOM / BG_HEIGHT) * b.bottom);
		msbBounds = new RectF(dpToPixels(left), dpToPixels(top),
				dpToPixels(right), dpToPixels(bottom));
		left = Math.round(((float) LSB_LEFT / BG_WIDTH) * b.right);
		top = Math.round(((float) LSB_TOP / BG_HEIGHT) * b.bottom);
		right = Math.round(((float) LSB_RIGHT / BG_WIDTH) * b.right);
		bottom = Math.round(((float) LSB_BOTTOM / BG_HEIGHT) * b.bottom);
		lsbBounds = new RectF(dpToPixels(left), dpToPixels(top),
				dpToPixels(right), dpToPixels(bottom));
		left = Math.round(((float) MULT_LEFT / BG_WIDTH) * b.right);
		top = Math.round(((float) MULT_TOP / BG_HEIGHT) * b.bottom);
		right = Math.round(((float) MULT_RIGHT / BG_WIDTH) * b.right);
		bottom = Math.round(((float) MULT_BOTTOM / BG_HEIGHT) * b.bottom);
		multiplierBounds = new RectF(dpToPixels(left), dpToPixels(top),
				dpToPixels(right), dpToPixels(bottom));
		left = Math.round(((float) TOL_LEFT / BG_WIDTH) * b.right);
		top = Math.round(((float) TOL_TOP / BG_HEIGHT) * b.bottom);
		right = Math.round(((float) TOL_RIGHT / BG_WIDTH) * b.right);
		bottom = Math.round(((float) TOL_BOTTOM / BG_HEIGHT) * b.bottom);
		toleranceBounds = new RectF(dpToPixels(left), dpToPixels(top),
				dpToPixels(right), dpToPixels(bottom));
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		paint.setStrokeWidth(0); // Single pixel stroke.
		paint.setColor(msb.getColor());
		canvas.drawRect(msbBounds, paint);
		paint.setColor(lsb.getColor());
		canvas.drawRect(lsbBounds, paint);
		paint.setColor(multiplier.getColor());
		canvas.drawRect(multiplierBounds, paint);
		paint.setColor(tolerance.getColor());
		canvas.drawRect(toleranceBounds, paint);

	}

	/**
	 * Increments the value of the most significant band.
	 */
	public void incMSB() {

		int value;
		if (msb != ResistorColor.WHITE) {
			value = msb.getValue();
			msb = ResistorColor.fromValue(value + 1);
			onValueChanged();
			this.postInvalidate(msbBounds.left, msbBounds.top, msbBounds.right,
					msbBounds.bottom);
		}
	}

	/**
	 * Decrements the value of the most significant band.
	 */
	public void decMSB() {

		int value;
		if (msb != ResistorColor.BLACK) {
			value = msb.getValue();
			msb = ResistorColor.fromValue(value - 1);
			onValueChanged();
			this.postInvalidate(msbBounds.left, msbBounds.top, msbBounds.right,
					msbBounds.bottom);
		}
	}

	/**
	 * Increments the value of the least significant band.
	 */
	public void incLSB() {

		int value;
		if (lsb != ResistorColor.WHITE) {
			value = lsb.getValue();
			lsb = ResistorColor.fromValue(value + 1);
			onValueChanged();
			this.postInvalidate(lsbBounds.left, lsbBounds.top, lsbBounds.right,
					lsbBounds.bottom);
		}
	}

	/**
	 * Decrements the value of the least significant band.
	 */
	public void decLSB() {

		int value;
		if (lsb != ResistorColor.BLACK) {
			value = lsb.getValue();
			lsb = ResistorColor.fromValue(value - 1);
			onValueChanged();
			this.postInvalidate(lsbBounds.left, lsbBounds.top, lsbBounds.right,
					lsbBounds.bottom);
		}
	}

	/**
	 * Increments the value of the multiplier band.
	 */
	public void incMultiplier() {

		int value;
		
		if (multiplier != ResistorColor.WHITE) {
			value = multiplier.getValue();
			multiplier = ResistorColor.fromValue(value + 1);
			onValueChanged();
			this.postInvalidate(multiplierBounds.left, multiplierBounds.top,
					multiplierBounds.right, multiplierBounds.bottom);
		}
	}

	/**
	 * Decrements the value of the multiplier band.
	 */
	public void decMultiplier() {

		int value;
		if (multiplier != ResistorColor.SILVER) {
			value = multiplier.getValue();
			multiplier = ResistorColor.fromValue(value - 1);
			onValueChanged();
			this.postInvalidate(multiplierBounds.left, multiplierBounds.top,
					multiplierBounds.right, multiplierBounds.bottom);
		}
	}

	/**
	 * Accessor for most significant band.
	 * @return		The color of the most significant band.
	 */
	public ResistorColor getMSB() {

		return msb;
	}
	
	/**
	 * Accessor for the boundary rectangle of the most significant band.
	 * @return		The boundary rectangle of the most significant band.
	 */
	public RectF getMSBBounds(){
		
		return msbBounds;
	}

	/**
	 * Acessor for the least significant band.
	 * @return		The color of the least significant band.
	 */
	public ResistorColor getLSB() {

		return lsb;
	}
	
	/**
	 * Accessor for the boundary rectangle of the least significant band.
	 * @return		Boundary rectangle of least significant band.
	 */
	public RectF getLSBBounds(){
		return lsbBounds;
	}

	/**
	 * Accessor for the multiplier band.
	 * @return		The color of the multiplier band.
	 */
	public ResistorColor getMultiplier() {

		return multiplier;
	}

	/**
	 * Accessor for the boundary rectangle of multiplier band.
	 * @return		The boundary rectanlge of multiplier band.
	 */
	public RectF getMultiplierBounds(){
		return multiplierBounds;
	}
	
	/**
	 * Determines if the point lies within the boundary's of the resistor bands.
	 * @param x		x-coordinate
	 * @param y		y-coordinate
	 * @return		The boundary rectangle that contains the point or null if the point is not
	 * 				contained by any band.
	 */
	public RectF collides(float x, float y){
		
		RectF bounds = null;
		if(msbBounds.contains(x,y)){
			bounds = msbBounds;
		}
		
		else if(lsbBounds.contains(x,y)){
			bounds = lsbBounds;
		}
		
		else if(multiplierBounds.contains(x,y)){
			bounds = multiplierBounds;
		}
		
		return bounds;
	}
	
	/**
	 * Mutator for multiplier band.
	 * @param color		The new band color.
	 */
	public void setMultiplier(ResistorColor color) {

		multiplier = color;
		this.postInvalidate(multiplierBounds.left, multiplierBounds.top,
				multiplierBounds.right, multiplierBounds.bottom);
		onValueChanged();
	}

	/**
	 * Mutator for most significant band.
	 * @param color		The new band color.
	 */
	public void setMSB(ResistorColor color) {
		msb = color;
		this.postInvalidate(msbBounds.left, msbBounds.top, msbBounds.right,
				msbBounds.bottom);
		onValueChanged();
	}

	/**
	 * Mutator for least significant band.
	 * @param color		The new band color
	 */
	public void setLSB(ResistorColor color) {
		lsb = color;
		this.postInvalidate(lsbBounds.left, lsbBounds.top, lsbBounds.right,
				lsbBounds.bottom);
		onValueChanged();
	}

	/**
	 * Accessor for the tolerance band
	 * @return		Always returns ResistorColor.GOLD
	 */
	public ResistorColor getTolerance() {

		return tolerance;
	}

	/**
	 * A convience method to convert float values to int values and call postInvalidate().
	 * @param left		Left coordinate of invalidated region.
	 * @param top		Top coordinate of invalidated region.
	 * @param right		Right coordinate of invalidated region.
	 * @param bottom	Bottom coordinate of invalidated region.
	 */
	private void postInvalidate(float left, float top, float right, float bottom) {
		super.postInvalidate(Math.round(left), Math.round(top),
				Math.round(right), Math.round(bottom));
	}

	/**
	 * Notifies clients when this resistor has it's bands changed.
	 */
	private void onValueChanged() {
		for (ResistorView.OnValueChangedListener l : onValueChangedListeners) {

			l.onValueChanged();
		}

	}

	/**
	 * Utility function to convert density independent pixels to screen pixels.
	 * @param dp
	 * @return
	 */
	private int dpToPixels(int dp) {

		return UnitConversion.dpToPixels(dp, SCALE);
	}


	/**
	 * Attaches an OnValueChangedListner to this ResistorView.
	 * @param l		The client.
	 */
	public void setOnValueChangedListner(ResistorView.OnValueChangedListener l) {

		onValueChangedListeners.add(l);
	}
}
