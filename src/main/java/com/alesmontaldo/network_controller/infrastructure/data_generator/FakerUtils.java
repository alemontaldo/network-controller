package com.alesmontaldo.network_controller.infrastructure.data_generator;

import java.util.Random;

public abstract class FakerUtils {

	public static final Random random = new Random();

	public static boolean randomBoolean() {
		return random.nextBoolean();
	}

	public static String getRandomWeather() {
		String[] weatherConditions = {
				"Sunny", "Cloudy", "Rainy", "Foggy", "Windy", "Snowy", "Clear skies",
				"Partly cloudy", "Overcast", "Light drizzle", "Heavy rain", "Thunderstorm",
				"Heatwave", "Chilly", "Freezing", "Mild", "Pleasant", "Humid"
		};
		return weatherConditions[random.nextInt(weatherConditions.length)];
	}

	public static String getRandomMood() {
		String[] moods = {
				"happy", "excited", "energetic", "motivated", "determined", "focused",
				"tired", "exhausted", "relaxed", "calm", "peaceful", "satisfied",
				"challenged", "struggling", "accomplished", "proud", "confident", "strong"
		};
		return moods[random.nextInt(moods.length)];
	}

	public static String getRandomQuote() {
		String[] quotes = {
				"Great effort!",
				"Keep pushing yourself!",
				"Impressive performance today.",
				"You're making good progress.",
				"That was a tough one!",
				"Nice work on maintaining your pace.",
				"You're getting stronger every day.",
				"Well done on completing this activity.",
				"This is becoming a good habit.",
				"Your consistency is admirable.",
				"You handled that challenge well.",
				"Your technique is improving.",
				"That's a personal best!",
				"You're inspiring others with your dedication.",
				"Good form throughout.",
				"You've come a long way since you started.",
				"That looked effortless!",
				"Your endurance is building nicely.",
				"You make it look easy!",
				"That's the spirit!",
				"Keep up the good work!",
				"You're on the right track.",
				"Your determination is paying off.",
				"That was a solid performance.",
				"You should be proud of yourself."
		};
		return quotes[random.nextInt(quotes.length)];
	}

}
