package com.hoshblok.TreeRender;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class TreeRender {

	private static int numberPosition = 1;
	private static String inputFileName;
	private static String outputFileName;
	private static Map<Integer, List<Number>> layersOfNumbersMap = new HashMap<>();

	public static void main(String[] args) {

		try {
			inputFileName = args[0];
			outputFileName = args[1];

		} catch (IndexOutOfBoundsException e) {
			System.out.println("Specify 2 launch parameters (file names)!");
			return;
		}

		try {
			String input = readFromFile(inputFileName);

			List<Number> numbers = parseNumbers(input);

			fillMap(layersOfNumbersMap, numbers);

			fillSpaceParameter(layersOfNumbersMap);

			writeToFile(outputFileName, buildTree(layersOfNumbersMap));

		} catch (FileNotFoundException e) {
			System.out.println("Input file is not found!");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String readFromFile(String filename) throws IOException {

		StringBuilder sb = new StringBuilder();

		try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {

			String line;

			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		}
		return sb.toString();
	}

	private static void writeToFile(String filename, String content) throws IOException {

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
			writer.write(content);
		}
	}

	private static void fillMap(Map<Integer, List<Number>> layersOfNumbersMap, List<Number> numberList) {

		for (Number number : numberList) {

			int layerNumber = number.getLayer();

			if (layersOfNumbersMap.get(layerNumber) == null) {
				layersOfNumbersMap.put(layerNumber, new ArrayList<>());
			}
			layersOfNumbersMap.get(layerNumber).add(number);
		}
	}

	private static void fillSpaceParameter(Map<Integer, List<Number>> layersOfNumbersMap) {

		for (List<Number> list : layersOfNumbersMap.values()) {

			Number maxNumber = list.stream().max(new Number.ValueComparator()).get();

			list.stream().forEach(n -> n.setSpace(maxNumber.getValue().toString().length() - 1));
		}
	}

	private static List<Number> convertMapToList(Map<Integer, List<Number>> layersOfNumbersMap) {

		return layersOfNumbersMap.values().stream().flatMap(List::stream).sorted(new Number.PositionComparator())
			.collect(Collectors.toList());
	}

	private static String buildTree(Map<Integer, List<Number>> layersOfNumbersMap) {

		StringBuilder sb = new StringBuilder();
		List<Number> checkList = convertMapToList(layersOfNumbersMap);
		List<Number> listOfNumbersByPostions = List.copyOf(checkList);

		int currentIndentation = 0;

		for (int i = 0; i < listOfNumbersByPostions.size(); i++) {

			sb.append("\n");

			for (int j = 1; j < listOfNumbersByPostions.get(i).layer; j++) {

				currentIndentation = setIndentation(listOfNumbersByPostions, currentIndentation, j);

				if (i == 0) {
					break;
				}
				appendIndentations(checkList, j, sb, currentIndentation);
			}

			sb.append(listOfNumbersByPostions.get(i).value);

			try {
				appendMinusPlus(listOfNumbersByPostions, i, sb);
			} catch (IndexOutOfBoundsException e) {
				continue;
			}

			checkList.remove(0);
		}

		return sb.substring(1).toString();
	}

	private static int setIndentation(List<Number> list, int currentIndentation, int index) {

		try {
			return currentIndentation = list.stream().filter(number -> number.layer == index).findAny().get().space;
		} catch (NoSuchElementException e) {
			return 0;
		}
	}

	private static void appendIndentations(List<Number> checkList, int index, StringBuilder sb,
		int currentIndentation) {

		if (index != 1 && checkList.stream().filter(number -> number.layer == index).findAny().isPresent()) {
			sb.append("|");
		} else {
			sb.append(" ");
		}
		sb.append(" ".repeat(3 + currentIndentation));
	}

	private static void appendMinusPlus(List<Number> list, int index, StringBuilder sb) {

		if (list.get(index + 1).layer > list.get(index).layer) {

			sb.append("-".repeat(3 + list.get(index).space - (list.get(index).value.toString().length() - 1)));

			sb.append("+");
		}
	}

	public static List<Number> parseNumbers(String input) {

		List<Number> numbers = new ArrayList<>();
		StringBuilder sb = new StringBuilder();

		parseNumbersRecursive(input, 0, 0, numbers, sb);
		return numbers;
	}

	private static int parseNumbersRecursive(String input, int startIndex, int layer, List<Number> numbers,
		StringBuilder sb) {

		int move = 0;

		for (int i = startIndex; i < input.length(); i++) {

			char character = input.charAt(i);
			// DIGIT
			if (Character.isDigit(character)) {
				move++;
				sb.append(character);
			}
			// OPEN
			if (character == '(') {
				move++;
				int numberOfSkippedIndexes = parseNumbersRecursive(input, i + 1, layer + 1, numbers, sb);
				i += numberOfSkippedIndexes;
				move += numberOfSkippedIndexes;
			}
			// CLOSE
			if (character == ')') {
				move++;
				if (sb.length() != 0) {
					addNumber(layer, numbers, sb);
				}
				return move;
			}
			// WHITESPACE
			if (character == ' ') {
				move++;
				if (sb.length() != 0) {
					addNumber(layer, numbers, sb);
				}
			}
		}
		return 0;
	}

	private static void addNumber(int layer, List<Number> numbers, StringBuilder sb) {

		numbers.add(new Number(Integer.valueOf(sb.toString()), layer, numberPosition));
		numberPosition++;
		sb.delete(0, sb.length());
	}

	// CLASS
	private static class Number {

		private Integer value;
		private int layer;
		private int space;
		private Integer position;

		public Number(int value, int layer, int position) {
			this.value = value;
			this.layer = layer;
			this.position = position;
		}

		public Integer getValue() {
			return value;
		}

		public int getLayer() {
			return layer;
		}

		public void setSpace(int space) {
			this.space = space;
		}

		// COMPORATORS
		private static class ValueComparator implements Comparator<Number> {

			@Override
			public int compare(Number obj1, Number obj2) {
				return obj1.value.compareTo(obj2.value);
			}
		}

		private static class PositionComparator implements Comparator<Number> {

			@Override
			public int compare(Number obj1, Number obj2) {
				return obj1.position.compareTo(obj2.position);
			}
		}
	}
}