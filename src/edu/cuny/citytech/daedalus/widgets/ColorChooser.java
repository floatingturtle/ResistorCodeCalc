/**
 * A helper class to manage the state of a menu drawn within a HeadsUpDisplay
 * 
 * @author Jason Warren
 * 
 */

package edu.cuny.citytech.daedalus.widgets;

import java.util.LinkedList;

import edu.cuny.citytech.daedalus.widgets.ResistorView.ResistorColor;
import android.graphics.RectF;

public class ColorChooser extends LinkedList<ResistorColor>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -897779196880995683L;
	private static final float BORDER_SIZE = 5;
	private RectF bounds;
	
	/**
	 * Constructs a new ColorChooser bounded by the rectangle specified by the input 
	 * coordinates.
	 * @param left		Left coordinate of bounding rectangle.
	 * @param top		Top coordinate of bounding rectangle.
	 * @param right		Right coordinate of bounding rectangle.
	 * @param bottom	Bottom coordinate of bounding rectangle.
	 */
	public ColorChooser(float left, float top, float right, float bottom){
		initalize(left,top, right, bottom);
	}
	
	/**
	 * Constructs a new ColorChooser bounded by the rectangle.
	 * 
	 * @param newBounds		Bounding rectangle for this ColorChooser
	 */
	public ColorChooser(RectF newBounds){
		initalize(newBounds.left,newBounds.top, newBounds.right, newBounds.bottom);
	}
	
	/**
	 * Called by constructor(s) to initialize data members.
	 * @param left		Left coordinate of bounding rectangle.
	 * @param top		Top coordinate of bounding rectangle.
	 * @param right		Right coordinate of bounding rectangle.
	 * @param bottom	Bottom coordinate of bounding rectangle.
	 */
	private void initalize(float left, float top, float right, float bottom){
		bounds = new RectF(left,top,right,bottom);
	}
	
	/**
	 * Accessor for this ColorChoosers bounds.
	 * @return		The bounds of this ColorChooser.
	 */
	public RectF getBounds(){
		return bounds;
	}
	
	/**
	 * Returns the bounds of the sub element at index.
	 * @param index		Index of element to retrieve bounds from.
	 * @return			Bounds of element at index.
	 */
	public RectF getBounds(int index){
		float left = BORDER_SIZE + index * cellSize();
		float right = left + cellSize();
		return new RectF(left, bounds.top + BORDER_SIZE, right, bounds.bottom - BORDER_SIZE);
	}
	
	/**
	 * Gets the bounds of the element that contains point (x,y)
	 * @param x		x-coordinate
	 * @param y		y-coordinate
	 * @return		The bounds of the containing element or null if no element contains the
	 * 				point.
	 */
	public RectF collides(float x, float y){
		
		RectF bounds = null;
		
		for(int i=0; i<this.size(); i++){
			if(this.getBounds(i).contains(x,y)){
				bounds = this.getBounds(i);
			}
		}
		
		return bounds;
	}
	
	/**
	 * Gets the index of the element that contains point (x,y)
	 * @param x		x-coordinate
	 * @param y		y-coordinate
	 * @return		Index of the element that contains (x,y) or -1 if not contained by any.
	 */
	public int collidesIndex(float x, float y){
		int index = -1;
		for(int i=0; i<this.size(); i++){
			if(this.getBounds(i).contains(x,y)){
				index = i;
			}
		}
		
		return index;
	}
	
	/**
	 * Returns the width of the boarder around this ColorChooser
	 * 
	 * @return		The width of the boarder around this ColorChooser.
	 */
	public float getBoarderWidth(){
		return BORDER_SIZE;
	}
	
	/**
	 * The size of each element.
	 * 
	 * @return		The size of each element.
	 */
	private float cellSize(){
		return (bounds.right - 2 * BORDER_SIZE) / super.size();
	}
	
}
