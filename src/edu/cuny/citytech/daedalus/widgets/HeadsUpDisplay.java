/**
 * This is a transparent overlay view that allows for drawing on top of other views. It
 * currently supports a menu for selecting a range of colors, and a simple text display.
 * 
 * @author Jason Warren
 * 
 */

package edu.cuny.citytech.daedalus.widgets;

import java.util.Arrays;
import edu.cuny.citytech.daedalus.utils.UnitConversion;
import edu.cuny.citytech.daedalus.widgets.ResistorView.ResistorColor;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

public class HeadsUpDisplay extends View {

	private final float SCALE;
	private Paint paint;
	private Rect myBounds;
	private int borderColor = Color.TRANSPARENT;
	private String text = "";
	private int textSize = 40;
	private PointF textPos;
	private Rect textBounds;
	private ColorChooser chooser;
	private int chooserBorderColor = Color.DKGRAY;
	private PointF anchor;
	
	
	/**
	 * Constructor
	 * 
	 * @param context
	 */
	public HeadsUpDisplay(Context context) {
		super(context);
		SCALE = getContext().getResources().getDisplayMetrics().density;
		initialize();
		
	}
	
	/**
	 * Taken from superclass documentation:
	 * Perform inflation from XML and apply a class-specific base style. This constructor of 
	 * View allows subclasses to use their own base style when they are inflating. For example, 
	 * a Button class's constructor would call this version of the super class constructor and 
	 * supply R.attr.buttonStyle for defStyle; this allows the theme's button style to modify 
	 * all of the base view attributes (in particular its background) as well as the Button 
	 * class's attributes.
	 * 
	 * @param context	The Context the view is running in, through which it can access the 
	 * current theme, resources, etc.
	 * @param attrs		The attributes of the XML tag that is inflating the view.
	 * @param defStyleAttr	An attribute in the current theme that contains a reference to a 
	 * style resource to apply to this view. If 0, no default style will be applied.
	 */
	public HeadsUpDisplay(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		SCALE = getContext().getResources().getDisplayMetrics().density;
		initialize();
		
	}

	/**
	 * Taken from superclass documentation:
	 * 
	 * Constructor that is called when inflating a view from XML. This is called when a view is 
	 * being constructed from an XML file, supplying attributes that were specified in the XML 
	 * file. This version uses a default style of 0, so the only attribute values applied are 
	 * those in the Context's Theme and the given AttributeSet.
	 * The method onFinishInflate() will be called after all children have been added.
	 * @param context	The Context the view is running in, through which it can access the 
	 * current theme, resources, etc.
	 * @param attrs		The attributes of the XML tag that is inflating the view.
	 */
	public HeadsUpDisplay(Context context, AttributeSet attrs) {
		super(context, attrs);
		SCALE = getContext().getResources().getDisplayMetrics().density;
		initialize();
		
	}
	
	/**
	 * Called by Constructors to initialize data members.
	 */
	private void initialize(){
		paint = new Paint();
		myBounds = new Rect();
		textBounds = new Rect();
		textPos = new PointF();
		anchor = new PointF();
		
	}
	
	@Override
	public void onLayout(boolean b, int left, int top, int right, int bottom){
		super.onLayout(b, left, top, right, bottom);
		
		getDrawingRect(myBounds);
		
	}
	
	@Override
	protected Parcelable onSaveInstanceState(){
		Bundle bundle = new Bundle();
		bundle.putParcelable("instanceState", super.onSaveInstanceState());
		bundle.putInt("borderColor", borderColor);
		bundle.putString("text", text);
		bundle.putInt("textSize", textSize);
		bundle.putFloat("textPosX", textPos.x);
		bundle.putFloat("textPosY", textPos.y);
		
		
		return bundle;
	}
	
	@Override
	public void onRestoreInstanceState(Parcelable state) {

		if (state instanceof Bundle) {
			Bundle bundle = (Bundle) state;
			borderColor = bundle.getInt("borderColor");
			text = bundle.getString("text");
			textSize = bundle.getInt("textSize");
			textPos = new PointF(bundle.getFloat("textPosX"), bundle.getFloat("textPosY"));
			
			state = bundle.getParcelable("instanceState");
		}

		super.onRestoreInstanceState(state);
	}

	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh){
		super.onSizeChanged(w, h, oldw, oldh);
		myBounds = new Rect(0,0,w,h);			
		float midX = myBounds.right/2f;
		float midY = myBounds.bottom/2f;
		textPos = new PointF(midX,midY);
		paint.getTextBounds(text, 0, text.length(), textBounds);
		textBounds.offsetTo((int)textPos.x, (int)textPos.y);
		postInvalidate();
	}

	@Override
	public void onDraw(Canvas canvas){
		
		super.onDraw(canvas);
		drawBorder(canvas);
		drawMsg(canvas);
		drawMenu(canvas);
		
	}
	
	/**
	 * Draws the chooser menu
	 * @param canvas	Canvas to draw the menu on.
	 */
	private void drawMenu(Canvas canvas) {
		float midX;
		float topY;
		float botY;
		String value;
		if(chooser != null){
			paint.setStrokeWidth(2);
			paint.setColor(chooserBorderColor);
			paint.setStyle(Paint.Style.FILL);
			canvas.drawRect(chooser.getBounds(),paint);
			paint.setStyle(Paint.Style.FILL);
			for(int i=0; i<chooser.size(); i++){
				paint.setShadowLayer(10, 5, 3, Color.BLACK);
				paint.setColor(chooser.get(i).getColor());
				canvas.drawRect(chooser.getBounds(i),paint);
				value = String.valueOf(chooser.get(i).getValue());
				if(value.equals("0")){
					paint.setShadowLayer(10, 5, 3, Color.WHITE);
				}
				canvas.drawText(value,chooser.getBounds(i).centerX(), 
						chooser.getBounds(i).centerY(), paint);
			}
			paint.setColor(chooserBorderColor);
			paint.setStrokeWidth(chooser.getBoarderWidth());
			midX = chooser.getBounds().centerX();
			topY = chooser.getBounds().top;
			botY = chooser.getBounds().bottom;
			if(chooser.getBounds().top > anchor.y){
				canvas.drawLine(midX, topY, anchor.x, anchor.y, paint);
			}
			
			else{
				canvas.drawLine(midX, botY, anchor.x, anchor.y, paint);
			}
		}
	}

	/**
	 * Draws the border around this HeadsUpDisplay
	 * @param canvas	Canvas to draw on.
	 */
	private void drawBorder(Canvas canvas) {
		paint.setStrokeWidth(2);
		paint.setColor(borderColor);
		paint.setStyle(Paint.Style.STROKE);
		paint.setShadowLayer(0, 0, 0, 0);
		canvas.drawRect(myBounds, paint);
		
	}

	/**
	 * Draws the message of this HeadsUpDisplay
	 * @param canvas	Canvas to draw on.
	 */
	private void drawMsg(Canvas canvas) {
		paint.setStrokeWidth(2);
		paint.setTextSize(UnitConversion.dpToPixels(textSize,SCALE));
		paint.setTextAlign(Align.CENTER);
		paint.setShadowLayer(10, 5, 3, Color.BLACK);
		canvas.drawText(text, textPos.x, textPos.y, paint);
	}

	/**
	 * Sets the text of this HeadsUpDisplay
	 * @param newText	The new text to display.
	 */
	public void setText(String newText){
		
		Rect union = new Rect(textBounds);
		text = newText;
		paint.getTextBounds(text, 0, text.length(), textBounds);		
		int x = (int)textPos.x;
		int y = (int)textPos.y;
		textBounds.offsetTo(x, y);
		union.union(textBounds);
		super.postInvalidate();
		//super.postInvalidate(union.left, union.top, union.right, union.bottom);
		
	}

	/**
	 * Creates a ColorChooser for a multiplier band. (All ResistorColor value used)
	 * @param left		The left coordinate for the ColorChoosers bounds.
	 * @param top		The top coordinate for the ColorChoosers bounds
	 * @param right		The right coordinate for the ColorChoosers bounds.
	 * @param bottom	The bottom coordinate for the ColorChoosers bounds.
	 */
	public void displayMultiplierChooser(float left, float top, float right, float bottom) {
		RectF bounds = new RectF(left, top, right, bottom);
		chooser = new ColorChooser(bounds);
		chooser.addAll(Arrays.asList(ResistorColor.values()));
		//TODO Invalidate more efficiently.
		postInvalidate();
		//postInvalidate(Math.round(bounds.left),Math.round(bounds.top),
			//	Math.round(bounds.right),Math.round(bounds.left));
	}
	
	/**
	 * Creates a chooser for a digit band (ResistorColor.GOLD and ResistorColor.SILVER omitted)
	 * 
	 * @param left		The left coordinate for the ColorChoosers bounds.
	 * @param top		The top coordinate for the ColorChoosers bounds
	 * @param right		The right coordinate for the ColorChoosers bounds.
	 * @param bottom	The bottom coordinate for the ColorChoosers bounds.
	 */
	public void displayChooser(float left, float top, float right, float bottom) {
		
		RectF bounds = new RectF(left,top,right,bottom);
		chooser = new ColorChooser(bounds);
		for(int i=2; i<ResistorColor.values().length; i++){
			chooser.add(ResistorColor.values()[i]);
		}
		postInvalidate();
	}
	
	/**
	 * Destroys the ColorChooser.
	 */
	public void dismissChooser(){
		chooser = null;
		//TODO invalidate more efficiently
		postInvalidate();
	}
	
	/**
	 * Returns the rectangle of the element that collides with point (x,y)
	 * @param x		x-coordinate
	 * @param y		y-coordinate
	 * @return		The rectangle of the element that collides with point (x,y)
	 */
	public RectF collides(float x, float y){
		
		RectF bounds = null;
		
		if(chooser != null){
			bounds = chooser.collides(x, y);
		}
		return bounds;
	}

	/**
	 * Gets the index of the element that contains point (x,y)
	 * @param x		x-coordinate
	 * @param y		y-coordinate
	 * @return		The index of the element that contains point (x,y)
	 */
	public int collidesIndex(float x, float y) {
		return chooser.collidesIndex(x, y);
	}

	/**
	 * When the ColorChooser is drawn, it will draw a line connecting itself to an anchor point
	 * to give visual indication as to which band the chooser is currently working for. This
	 * method sets that anchor point.
	 * @param rawX		raw x-coordinate of anchor point
	 * @param rawY		raw y-coordinate of anchor point
	 */
	public void setAnchor(float rawX, float rawY) {
		anchor.x = rawX;
		anchor.y = rawY;
	}
	
	/**
	 * Gets the color of the element at index.
	 * @param index		The element who's color to retrieve.
	 * @return			The color of the element.
	 */
	public ResistorColor getColor(int index){
		return chooser.get(index);
	}
	
}
