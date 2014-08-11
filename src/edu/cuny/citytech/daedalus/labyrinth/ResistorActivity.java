/**
 * The resistor calculator activity.
 * 
 * @author Jason Warren
 * 
 */

package edu.cuny.citytech.daedalus.labyrinth;

import java.math.BigDecimal;
import java.math.MathContext;

import edu.cuny.citytech.daedalus.utils.Resistance;
import edu.cuny.citytech.daedalus.widgets.HeadsUpDisplay;
import edu.cuny.citytech.daedalus.widgets.ResistorView;
import edu.cuny.citytech.daedalus.widgets.ResistorView.ResistorColor;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class ResistorActivity extends ActionBarActivity implements
		ResistorView.OnValueChangedListener, TextWatcher, OnClickListener,
		OnTouchListener, OnEditorActionListener {
	
	private enum ResistorBand{
		MSB, LSB, MULTIPLIER, TOLERANCE
	}

	private final char OMEGA = '\u03A9';
	private final char NOT_EQUAL = '\u2260';
	private static final int MEGA = 1000000;
	private static final int KILO = 1000;
	private static final MathContext PRECISION = new MathContext(2);
	private ResistorView resistor;
	private EditText msg;
	private HeadsUpDisplay hud;
	private boolean clearTextOnTouch = true;
	private boolean fromTextToBands = true; // True when we are updating
											// resistor bands based on text input.
	private boolean restored;
	private int restoreColor;
	private ResistorBand selected = null;
	private Toast toasty;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_resistor);

		// The activity is not being recreated from a previous state.
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new ResistorFragment()).commit();
		}

		else {
			restored = true;
			clearTextOnTouch = savedInstanceState.getBoolean("firstInput");
			fromTextToBands = savedInstanceState.getBoolean("fromTextToBands");
			restoreColor = savedInstanceState.getInt("msgColor");
		}
	}

	@Override
	protected void onStart() {

		super.onStart();

		// Get the root view of the fragment.
		FragmentManager fragManage = getSupportFragmentManager();
		Fragment f = fragManage.findFragmentById(R.id.container);
		View rootView = f.getView();

		resistor = (ResistorView) rootView.findViewById(R.id.resistorView1);
		resistor.setOnValueChangedListner(this);
		resistor.setOnTouchListener(this);

		msg = (EditText) rootView.findViewById(R.id.editText1);
		msg.setOnClickListener(this);
		if(restored){
			msg.setTextColor(restoreColor);
		}
		msg.addTextChangedListener(this);
		msg.setOnEditorActionListener(this);
		
		//toasty = Toast.makeText(getApplicationContext(),"", Toast.LENGTH_SHORT);
		hud = (HeadsUpDisplay) rootView.findViewById(R.id.headsUpDisplay1);
		hud.setOnTouchListener(this);
		
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {

		savedInstanceState.putBoolean("clearTextOnTouch", clearTextOnTouch);
		savedInstanceState.putBoolean("fromTextToBands", fromTextToBands);
		savedInstanceState.putInt("msgColor", msg.getCurrentTextColor());

		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.resistor, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class ResistorFragment extends Fragment {

		public ResistorFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_resistor,
					container, false);

			return rootView;
		}

	}

	@Override
	public void onValueChanged() {

		BigDecimal resistance = calculateResistance(
				resistor.getMSB(), resistor.getLSB(), resistor.getMultiplier(),
				resistor.getTolerance());
		String resistanceString;

		if (!fromTextToBands) {
			if(resistor.getMSB() != ResistorColor.BLACK){
				resistanceString = toEngineeringNotation(resistance);
				msg.setText(resistanceString);
				msg.append(new String(new char[] { OMEGA }));
				clearTextOnTouch = false;
				if(Resistance.isStandard(resistanceString)){
					msg.setTextColor(Color.GREEN);
					toast("Standard", Color.WHITE, Color.GREEN);
				}
				
				else{
					msg.setTextColor(Color.BLACK);
					toast("Non-standard", Color.WHITE, Color.BLACK);
				}
			}
			else{
				clearTextOnTouch = true;
				msg.setText("1st band " + NOT_EQUAL +" black");
				msg.setTextColor(Color.RED);
				toast("Invalid ResistorCode", Color.WHITE, Color.RED);
			}
		}

	}

	/**
	 * Converts a resistance string to engineering notation.
	 * @param resistance
	 * @return
	 */
	private String toEngineeringNotation(BigDecimal resistance) {
		
		String resistanceString;
		BigDecimal mega = new BigDecimal(MEGA);
		BigDecimal kilo = new BigDecimal(KILO);
		
		if(resistance.compareTo(mega) >= 0){
			
			resistance = resistance.divide(mega, PRECISION);
			resistanceString = resistance.toPlainString() + "M";
		}
		
		else if(resistance.compareTo(kilo) >= 0){
			
			resistance = resistance.divide(kilo, PRECISION);
			resistanceString = resistance.toPlainString() + "k";
		}
		
		else{
			resistanceString = resistance.round(PRECISION).toPlainString();
		}
		
		return resistanceString;
	}

	private BigDecimal calculateResistance(ResistorView.ResistorColor msb,
			ResistorView.ResistorColor lsb, ResistorView.ResistorColor mult,
			ResistorView.ResistorColor tol) {
		
		BigDecimal value = new BigDecimal((msb.getValue() * 10 + lsb.getValue())
				* Math.pow(10, mult.getValue()));

		return value;
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {

	}

	@Override
	public void afterTextChanged(Editable s) {

		if (fromTextToBands) {
			setResistanceBands(s.toString());
		}
	}

	/**
	 * Sets the bands of the ResistorView to the value of resistance.
	 * @param resistance	Resistance value.
	 */
	private void setResistanceBands(String resistance) {

		// Determine if the string is valid.
		ResistorColor msbColor = null;
		ResistorColor lsbColor = null;
		ResistorColor multColor = null;
		BigDecimal resistanceValue = Resistance.parse(resistance);
		int exponent;
		int firstSignificantDigit;
		int secondSignificantDigit;

		if (resistanceValue.compareTo(new BigDecimal(0)) > 0) {
			
			exponent = Resistance.getMultiplier(resistance);

			if(exponent > -3 && exponent < 10){
				multColor = ResistorColor.fromValue(exponent);
				firstSignificantDigit = Resistance.getFirstSigDigit(resistance);
				msbColor = ResistorColor.fromValue(firstSignificantDigit);				
				secondSignificantDigit = Resistance.getSecondSigDigit(resistance);
				lsbColor = ResistorColor.fromValue(secondSignificantDigit);
				resistor.setMSB(msbColor);
				resistor.setLSB(lsbColor);
				resistor.setMultiplier(multColor);
				if(Resistance.isStandard(resistance)){
					msg.setTextColor(Color.GREEN);
				}
				else{
					msg.setTextColor(Color.BLACK);
				}
			}
			
			else{
				badResistance();
			}
		}

		else {
			badResistance();		
		}
	}

	/**
	 * Indicates that the resistance value is not valid.
	 */
	private void badResistance() {
		msg.setTextColor(Color.RED);
		resistor.setMSB(ResistorColor.BLACK);
		resistor.setLSB(ResistorColor.BLACK);
		resistor.setMultiplier(ResistorColor.BLACK);
	}

	/**
	 * Display a colored toast message. Toasty!
	 * @param text		The text of the toast.
	 * @param textColor	The color of the text.
	 * @param bgColor	The color of the background.
	 */
	private void toast(String text, int textColor, int bgColor) {
		if(toasty != null){
			toasty.cancel();
		}
		toasty = Toast.makeText(getApplicationContext(),text, Toast.LENGTH_SHORT);
		TextView toastyTextView;
		toasty.getView().setBackgroundColor(bgColor);
		toastyTextView = (TextView)toasty.getView().findViewById(android.R.id.message);
		toastyTextView.setTextColor(textColor);
		toasty.show();
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.editText1) {
			fromTextToBands = true;
			if (clearTextOnTouch) {
				msg.setText("");
				clearTextOnTouch = false;
			}
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		
		float width;
		float height;
		float x;
		float y;
		RectF bounds;
		ResistorColor color;
		int index;
		if (v.getId() == R.id.resistorView1) {
			fromTextToBands = false;
			bounds = resistor.collides(event.getX(),event.getY());
			if( bounds != null){
				width = hud.getWidth();
				height = hud.getWidth() / 9;
				x = 0;
				if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
					
					y = v.getBottom() + v.getHeight() / 2;
				}
				
				else{
					y = v.getTop() - v.getHeight();
				}
				
				hud.setAnchor(v.getLeft() + bounds.centerX(), v.getTop() + bounds.centerY());
				
				if(resistor.getMSBBounds().contains(event.getX(), event.getY())){
					selected = ResistorBand.MSB;
					hud.displayChooser(x,y,x+width, y+height);
				}
				
				else if(resistor.getLSBBounds().contains(event.getX(), event.getY())){
					selected = ResistorBand.LSB;
					hud.displayChooser(x,y,x+width, y+height);
				}
				
				else if(resistor.getMultiplierBounds().contains(event.getX(), event.getY())){
					selected = ResistorBand.MULTIPLIER;
					hud.displayMultiplierChooser(x,y,x+width, y+height);
				}
			}
		}
		
		else if(v.getId() == R.id.headsUpDisplay1){
			bounds = hud.collides(event.getX(), event.getY());
			
			if(bounds != null){
				index = hud.collidesIndex(event.getX(), event.getY());
				color = hud.getColor(index);
				if(selected == ResistorBand.MSB){
					resistor.setMSB(color);
					selected = null;
					hud.dismissChooser();
				}
				
				else if(selected == ResistorBand.LSB){
					resistor.setLSB(color);
					selected = null;
					hud.dismissChooser();
				}
				
				else if(selected == ResistorBand.MULTIPLIER){
					resistor.setMultiplier(color);
					selected = null;
					hud.dismissChooser();
				}
				
			}
			
			else{
				selected = null;
				hud.dismissChooser();
			}
		}

		v.performClick();
		
		return false;
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if(actionId == EditorInfo.IME_ACTION_DONE){
			String resistance = msg.getText().toString();
			int multiplier = Resistance.getMultiplier(resistance);
			
			if(Resistance.isStandard(resistance) && multiplier > - 3 && multiplier < 10){
				toast("Standard", Color.WHITE, Color.GREEN);
			}
			
			else if(Resistance.isValid(resistance) && multiplier > -3 && multiplier < 10){
				toast("Non-standard", Color.WHITE, Color.BLACK);
				
			}
		
			else if(!clearTextOnTouch && !resistance.equals("")){
				toast("Invalid Resistance", Color.WHITE, Color.RED);
			}
		}
		return false;
	}

}
