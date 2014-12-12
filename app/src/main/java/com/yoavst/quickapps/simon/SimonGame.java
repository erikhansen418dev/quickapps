package com.yoavst.quickapps.simon;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Yoav.
 */
public class SimonGame {
	static Random random = new Random();

	ArrayList<Color> colors = new ArrayList<>();
	ArrayList<Color> ongoingColors;
	int position;

	enum Color {
		Red, Blue, Green, Yellow;

		public static Color generate() {
			return generateFrom(random.nextInt(3));
		}

		public static Color generateFrom(int num) {
			switch (num) {
				case 0:
					return Red;
				case 1:
					return Blue;
				case 2:
					return Green;
				default:
				case 3:
					return Yellow;
			}
		}
	}

	ArrayList<Color> generateNext() {
		position = 0;
		ongoingColors = new ArrayList<>();
		colors.add(Color.generate());
		return colors;
	}

	Boolean press(Color color) {
		if (colors.size() <= ongoingColors.size()) return null;
		else {
			ongoingColors.add(color);
			boolean b = color == colors.get(position);
			position++;
			return b && colors.size() <= position ? null : b;
		}
	}

	int getRound() {
		return colors.size();
	}
}
