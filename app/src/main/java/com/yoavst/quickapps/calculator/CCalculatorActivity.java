package com.yoavst.quickapps.calculator;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lge.qcircle.template.QCircleTemplate;
import com.lge.qcircle.template.TemplateTag;
import com.yoavst.quickapps.App;
import com.yoavst.quickapps.Expression;
import com.yoavst.quickapps.Preferences_;
import com.yoavst.quickapps.QCircleActivity;
import com.yoavst.quickapps.R;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;

/**
 * Created by Yoav.
 */
public class CCalculatorActivity extends QCircleActivity {
	private static final String OPEN_BRACKET = "(";
	private static final String CLOSE_BRACKET = ")";
	private static final String INFINITY = "\u221E";
	TextView text;
	TextView answer;
	String DOT;
	String PLUS;
	String MINUS;
	String MULTIPLE;
	String DIVIDE;
	String POW;
	String ERROR;
	String[] OPERATORS;
	boolean showingAnswer = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		template = new QCircleTemplate(this);
		template.setBackButton();
		RelativeLayout main = template.getLayoutById(TemplateTag.CONTENT_MAIN);
		LinearLayout layout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.calculator_circle_layout, main, false);
		main.addView(layout);
		setContentView(template.getView());
		init(layout);
	}

	@Override
	protected Intent getIntentToShow() {
		return App.createExplicitFromImplicitIntent(this, new Intent().setClassName("com.android.calculator2",
				"com.android.calculator2.Calculator").putExtra("com.lge.app.floating.launchAsFloating", new Preferences_(this).calculatorForceFloating().get()));
	}

	private void init(LinearLayout layout) {
		text = (TextView) layout.findViewById(R.id.text_line);
		answer = (TextView) layout.findViewById(R.id.answer_line);
		DOT = getString(R.string.dot);
		PLUS = getString(R.string.plus);
		MINUS = getString(R.string.minus);
		MULTIPLE = getString(R.string.mul);
		DIVIDE = getString(R.string.div);
		POW = getString(R.string.exponentiation);
		ERROR = getString(R.string.error);
		OPERATORS = getResources().getStringArray(R.array.operators);
		for (int id : new int[]{R.id.digit0, R.id.digit1, R.id.digit2, R.id.digit3,
				R.id.digit4, R.id.digit5, R.id.digit6, R.id.digit7, R.id.digit8, R.id.digit9, R.id.dot}) {
			findViewById(id).setOnClickListener(this::onNumberClicked);
		}
		for (int id : new int[]{R.id.div, R.id.exponentiation, R.id.mul, R.id.minus,
				R.id.plus}) {
			findViewById(id).setOnClickListener(this::onOperatorClicked);
		}
		findViewById(R.id.del).setOnClickListener(v -> deleteLastChar());
		findViewById(R.id.paren).setOnClickListener(v -> onBracketsClicked());
		findViewById(R.id.allClear).setOnClickListener(v -> clearAll());
		findViewById(R.id.del).setOnLongClickListener(v -> {
			clearAll();
			return true;
		});
		findViewById(R.id.equal).setOnClickListener(v -> compute());

	}

	void onNumberClicked(View view) {
		cleanAnswer(false);
		if (!view.getTag().equals(DOT))
			text.append((CharSequence) view.getTag());
		else {
			String text = this.text.getText().toString();
			if (text.length() == 0) this.text.append("0" + DOT);
			else if (isOperator(getLastChar()) || getLastChar().equals(OPEN_BRACKET))
				this.text.append("0" + DOT);
			else if (!text.contains(DOT)) this.text.append(DOT);
			else {
				int lastIndex = text.length() - 1;
				String c;
				do {
					c = new String(new char[]{text.charAt(lastIndex)});
					if (c.equals(DOT)) return;
					lastIndex--;
				} while (!(isOperator(c) || lastIndex == -1));
				this.text.append(DOT);
			}
		}
	}

	void onOperatorClicked(View view) {
		cleanAnswer(true);
		if (text.length() == 0) {
			if (view.getTag().equals(MINUS))
				text.append((CharSequence) view.getTag());
		} else if (!isOperator(getLastChar())) {
			String lastChar = getLastChar();
			if ((!lastChar.equals(OPEN_BRACKET) && !lastChar.equals(CLOSE_BRACKET)))
				text.append((CharSequence) view.getTag());
			else if (view.getTag().equals(MINUS))
				text.append((CharSequence) view.getTag());
		} else if (text.length() != 1) {
			String charBefore = getLastChar();
			String twoCharBefore = new String(new char[]{text.getText().charAt(text.length() - 2)});
			if ((!charBefore.equals(OPEN_BRACKET) && !twoCharBefore.equals(OPEN_BRACKET)) ||
					view.getTag().equals(MINUS)) {
				if (!charBefore.equals(POW) || view.getTag().toString().equals(DIVIDE) ||
						view.getTag().toString().equals(MULTIPLE) || view.getTag().toString().equals(PLUS) ||
						view.getTag().equals(POW))
					deleteLastChar();
				text.append((CharSequence) view.getTag());
			}
		}

	}

	void deleteLastChar() {
		answer.setText(null);
		if (text.length() != 0) {
			text.setText(text.getText().toString().substring(0, text.getText().toString().length() - 1));
		}
	}

	void onBracketsClicked() {
		cleanAnswer(true);
		int numberOfOpenBrackets = calcOpenBrackets();
		if (numberOfOpenBrackets == 0) {
			if (text.length() == 0 || isOperator(getLastChar()))
				text.append(OPEN_BRACKET);
			else text.append(MULTIPLE + OPEN_BRACKET);
		} else {
			String lastChar = text.getText().toString().substring(text.getText().toString().length() - 1);
			if (isOperator(lastChar) || lastChar.equals(OPEN_BRACKET)) {
				text.append(OPEN_BRACKET);
			} else if (lastChar.equals(CLOSE_BRACKET)) {
				text.append(MULTIPLE + OPEN_BRACKET);
			} else {
				text.append(CLOSE_BRACKET);
			}
		}

	}

	void clearAll() {
		text.setText(null);
		answer.setText(null);
	}

	void compute() {
		if (text.length() != 0 && !showingAnswer) {
			String math = fixFormat(removeLastOperator(addMissingBrackets(text.getText().toString())));
			Log.v("Calculator", math);
			try {
				Expression expression = new Expression(math).setPrecision(100);
				BigDecimal decimal = expression.eval().stripTrailingZeros();
				int numberOfDigits = numberOfDigits(decimal);
				String text;
				if (numberOfDigits >= 14) {
					// Force scientific notation but check if the regular toString has different value
					text = new DecimalFormat("0.00E00").format(decimal);
					String[] parts = text.split("E");
					if (parts[0].equals("1.00")) text = "10" + POW + parts[1];
					else {
						text = formatNumber(decimal);
					}
				} else if (numberOfDigits >= 7) {
					// Allow scientific notation, but do not force.
					text = decimal.toString()
							// 1E+7 to 10^7
							.replace("1E+", "10" + POW)
									// 2E+7 to 2x10^7
							.replace("E+", MULTIPLE + "10" + POW);
				} else if (decimal.compareTo(BigDecimal.ONE) >= 0 || decimal.compareTo(new BigDecimal("-1")) <= 0)
					// Force regular decimal formatting
					text = decimal.toPlainString();
				else {
					int numberOfUnscaledDigits = decimal.scale();
					if (numberOfUnscaledDigits < 7)
						// Force regular decimal formatting
						text = decimal.toPlainString();
					else if (numberOfUnscaledDigits < 14) {
						// Allow scientific notation, but do not force.
						text = decimal.toString()
								// 1E-7 to 10^-7
								.replace("1E-", "10" + POW + MINUS)
										// 2E-7 to 2x10^-7
								.replace("E+", MULTIPLE + "10" + POW + MINUS);
					} else if (numberOfUnscaledDigits == 100) {
						text = decimal.toPlainString();
					} else
						text = formatNumber(decimal);

				}
				answer.setText(text);
			} catch (RuntimeException e) {
				if (e.getMessage() != null && e.getMessage().toLowerCase().contains("division by zero")) {
					answer.setText(INFINITY);
				} else answer.setText(ERROR);
				e.printStackTrace();
			}
			showingAnswer = true;
		}
	}

	private void cleanAnswer(boolean copyAnswer) {
		if (showingAnswer) {
			String answer = this.answer.getText().toString();
			this.answer.setText(null);
			if (copyAnswer && !(answer.contains(ERROR) || answer.contains(INFINITY))) {
				text.setText(answer.length() == 14 ? formatNumber(new BigDecimal(answer)) : answer);
			} else text.setText(null);
			showingAnswer = false;
		}
	}

	private int calcOpenBrackets() {
		String text = this.text.length() == 0 ? "" : this.text.getText().toString();
		char open = OPEN_BRACKET.charAt(0);
		char close = CLOSE_BRACKET.charAt(0);
		int numberOfOpen = 0;
		for (char c : text.toCharArray()) {
			if (c == open) numberOfOpen++;
			else if (c == close) numberOfOpen--;
		}
		return numberOfOpen;
	}

	private String addMissingBrackets(String original) {
		int openBrackets = calcOpenBrackets();
		if (openBrackets == 0) return original;
		else {
			StringBuilder builder = new StringBuilder(original);
			while (openBrackets != 0) {
				builder.append(CLOSE_BRACKET);
				openBrackets--;
			}
			return builder.toString();
		}
	}

	private String fixFormat(String original) {
		if (original == null || original.length() == 0) return original;
		else {
			if (original.startsWith(MINUS + OPEN_BRACKET))
				original = original.replaceFirst(MINUS + "\\" + OPEN_BRACKET, MINUS + "1" + MULTIPLE + OPEN_BRACKET);
			original = original.replace(MULTIPLE, "*").replace(DIVIDE, "/");
			return original;
		}
	}

	private String removeLastOperator(String original) {
		if (original == null || original.length() == 0 || !isOperator(getLastChar()))
			return original;
		else return original.substring(0, original.length() - 1);
	}

	private String getLastChar() {
		return new String(new char[]{text.getText().charAt(text.length() - 1)});
	}

	private boolean isOperator(CharSequence string) {
		if (string != null) {
			for (String operator : OPERATORS) {
				if (operator.contentEquals(string)) return true;
			}
		}
		return false;
	}

	private String formatNumber(BigDecimal decimal) {
		String text = new DecimalFormat("0.00E00").format(decimal);
		String[] parts = text.split("E");
		if (parts[0].equals("1.00")) text = "10" + POW + parts[1];
		else {
			// Deal with part 1
			double value = Double.parseDouble(parts[0]);
			if (value == (int) value)
				text = String.valueOf((int) value) + MULTIPLE + "10" + POW;
			else if (value * 10 == (int) value * 10)
				text = String.valueOf(((int) value * 10) / 10) + DOT +
						String.valueOf(((int) value * 10) % 10) + MULTIPLE + "10" + POW;
			else text = String.valueOf(value) + MULTIPLE + "10" + POW;
			// Deal with part 2
			double val = Double.parseDouble(parts[1]);
			if (val == (int) val)
				text += String.valueOf((int) val);
			else if (val * 10 == (int) val * 10)
				text += String.valueOf(((int) val * 10) / 10) + DOT +
						String.valueOf(((int) val * 10) % 10);
			else text += String.valueOf(val);
		}
		return text;
	}

	private static int numberOfDigits(BigDecimal bigDecimal) {
		return numberOfDigits(bigDecimal.toBigInteger());
	}

	private static int numberOfDigits(BigInteger digits) {
		BigInteger ten = BigInteger.valueOf(10);
		int count = 0;
		do {
			digits = digits.divide(ten);
			count++;
		} while (!digits.equals(BigInteger.ZERO));
		return count;
	}
}
