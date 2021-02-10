package com.thelogicmaster.terrible_calculator;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Main extends JFrame {

	private String currentNum;
	private String firstNum;
	private char operator;
	private State state;
	private String result;
	private final JLabel output;
	private final ScriptEngine engine;

	public Main () {
		super("Terrible Calculator");

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;

		currentNum = "";
		state = State.First;
		engine = new ScriptEngineManager().getEngineByName("JavaScript");

		output = new JLabel("0");
		output.setHorizontalAlignment(SwingConstants.RIGHT);
		output.setForeground(Color.DARK_GRAY);
		c.gridwidth = 3;
		c.insets = new Insets(1, 1, 1, 5);
		add(output, c);
		c.gridwidth = 1;
		c.insets.right = 1;

		JButton[] numButtons = new JButton[10];
		for (int i = 0; i < 10; i++) {
			numButtons[i] = new JButton("" + i);
			final int num = i;
			numButtons[i].addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked (MouseEvent e) {
					if (state == State.Result) {
						state = State.First;
						currentNum = "";
					}
					if ("0".equals(currentNum))
						currentNum = "";
					currentNum += num;
					displayOutput();
				}
			});
			if (i == 0) {
				c.gridx = 1;
				c.gridy = 4;
			} else {
				c.gridx = (i - 1) % 3;
				c.gridy = 1 + (i - 1) / 3;
			}

			add(numButtons[num], c);
		}

		char[] operators = {'+', '-', '*', '/'};
		JButton[] opButtons = new JButton[4];
		for (int i = 0; i < 4; i++) {
			opButtons[i] = new JButton("" + operators[i]);
			final char op = operators[i];
			opButtons[i].addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked (MouseEvent e) {
					switch (state) {
					case First:
						if ("".equals(currentNum))
							return;
						state = State.Second;
						firstNum = currentNum;
						currentNum = "";
					case Second:
						operator = op;
						break;
					case Result:
						if (result == null)
							return;
						state = State.Second;
						currentNum = "";
						firstNum = "" + result;
						operator = op;
						break;
					}
					displayOutput();
				}
			});
			c.gridx = 3;
			c.gridy = 1 + i;
			add(opButtons[i], c);
		}

		JButton clearButton = new JButton("CE");
		clearButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked (MouseEvent e) {
				state = State.First;
				currentNum = "";
				displayOutput();
			}
		});
		c.gridx = 0;
		c.gridy = 5;
		add(clearButton, c);

		JButton decimalButton = new JButton(".");
		decimalButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked (MouseEvent e) {
				if (state == State.Result || currentNum.contains("."))
					return;
				currentNum += ".";
			}
		});
		c.gridx = 2;
		c.gridy = 4;
		add(decimalButton, c);

		JButton negativeButton = new JButton("-");
		negativeButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked (MouseEvent e) {
				if ("".equals(currentNum) || state == State.Result)
					return;
				currentNum = "" + (Integer.decode(currentNum) * -1);
				displayOutput();
			}
		});
		c.gridx = 0;
		c.gridy = 4;
		add(negativeButton, c);

		JButton enterButton = new JButton("Enter");
		enterButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked (MouseEvent e) {
				if ("".equals(currentNum) || state == State.First)
					return;
				if (state == State.Result)
					firstNum = result;
				result = evaluate();
				state = State.Result;
				displayOutput();
			}
		});
		c.gridx = 1;
		c.gridy = 5;
		c.gridwidth = 3;
		add(enterButton, c);

		JButton colorButton = new JButton("Color");
		colorButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked (MouseEvent e) {
				Color color = JColorChooser.showDialog(Main.this, "", output.getBackground(), false);
				output.setForeground(color);
				colorButton.setForeground(color);
				negativeButton.setForeground(color);
				decimalButton.setForeground(color);
				clearButton.setForeground(color);
				enterButton.setForeground(color);
				for (JButton button : numButtons)
					button.setForeground(color);
				for (JButton button : opButtons)
					button.setForeground(color);
			}
		});
		c.gridx = 3;
		c.gridy = 0;
		add(colorButton, c);

		pack();
		setSize(400, 250);
		setVisible(true);
	}

	private String evaluate () {
		try {
			Object calculated = engine.eval(firstNum + operator + currentNum);
			if (calculated instanceof Integer)
				return "" + calculated;
			else if (calculated instanceof Double)
				return "" + Math.round((double)calculated * 100f) / 100f;
		} catch (ScriptException scriptException) {
			scriptException.printStackTrace();
		}
		return null;
	}

	private void displayOutput () {
		switch (state) {
		case First:
			output.setText("".equals(currentNum) ? "0" : currentNum);
			break;
		case Second:
			output.setText(firstNum + " " + operator + " " + currentNum);
			break;
		case Result:
			output.setText(result == null ? "Error" : firstNum + " " + operator + " " + currentNum + " = " + result);
		}
	}

	private enum State {
		First,
		Second,
		Result
	}

	public static void main (String[] args) {
		new Main();
	}
}
