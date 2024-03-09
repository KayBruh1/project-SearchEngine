package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Outputs several simple data structures in "pretty" JSON format where newlines
 * are used to separate elements and nested elements are indented using spaces.
 *
 * Warning: This class is not thread-safe. If multiple threads access this class
 * concurrently, access must be synchronized externally.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2024
 */
public class JsonWriter {
	/**
	 * Indents the writer by the specified number of times. Does nothing if the
	 * indentation level is 0 or less.
	 *
	 * @param writer the writer to use
	 * @param indent the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeIndent(Writer writer, int indent) throws IOException {
		while (indent-- > 0) {
			writer.write("  ");
		}
	}

	/**
	 * Indents and then writes the String element.
	 *
	 * @param element the element to write
	 * @param writer  the writer to use
	 * @param indent  the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeIndent(String element, Writer writer, int indent) throws IOException {
		writeIndent(writer, indent);
		writer.write(element);
	}

	/**
	 * Indents and then writes the text element surrounded by {@code " "} quotation
	 * marks.
	 *
	 * @param element the element to write
	 * @param writer  the writer to use
	 * @param indent  the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeQuote(String element, Writer writer, int indent) throws IOException {
		writeIndent(writer, indent);
		writer.write('"');
		writer.write(element);
		writer.write('"');
	}

	/**
	 * Writes the elements as a pretty JSON array.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param indent   the initial indent level; the first bracket is not indented,
	 *                 inner elements are indented by one, and the last bracket is
	 *                 indented at the initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 */
	public static void writeArray(Collection<? extends Number> elements, Writer writer, int indent) throws IOException {
		writer.write("[");
		var iterator = elements.iterator();
		if (iterator.hasNext()) {
			Number value = iterator.next();
			writer.write("\n");
			writeIndent(value.toString(), writer, indent + 1);
		}
		while (iterator.hasNext()) {
			Number value = iterator.next();
			writer.write(",");
			writer.write("\n");
			writeIndent(value.toString(), writer, indent + 1);
		}
		if (!elements.isEmpty()) { // TODO This check is unecessary, always output the \n below. Fix everywhere!
			writer.write("\n");
			writeIndent(writer, indent);
		}
		writer.write("]");
	}

	/**
	 * Writes the elements as a pretty JSON array to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeArray(Collection, Writer, int)
	 */
	public static void writeArray(Collection<? extends Number> elements, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeArray(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON array.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeArray(Collection, Writer, int)
	 */
	public static String writeArray(Collection<? extends Number> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeArray(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the elements as a pretty JSON object.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param indent   the initial indent level; the first bracket is not indented,
	 *                 inner elements are indented by one, and the last bracket is
	 *                 indented at the initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 */
	public static void writeObject(Map<String, ? extends Number> elements, Writer writer, int indent)
			throws IOException {
		writer.write("{");
		var iterator = elements.entrySet().iterator();
		Map.Entry<String, ? extends Number> entry = null;
		String key = null;
		Number value = null;
		writer.write("\n");

		if (iterator.hasNext()) {
			entry = iterator.next();
			key = entry.getKey();
			value = entry.getValue();
			writeIndent(writer, indent + 1);
			writeQuote(key, writer, 0);
			writer.write(": ");
			writeIndent(value.toString(), writer, 0);
		}

		while (iterator.hasNext()) {
			writer.write(",");
			writer.write("\n");
			entry = iterator.next();
			key = entry.getKey();
			value = entry.getValue();
			writeIndent(writer, indent + 1);
			writeQuote(key, writer, 0);
			writer.write(": ");
			writeIndent(value.toString(), writer, 0);
		}
		if (!elements.isEmpty()) {
			writer.write("\n");
			writeIndent(writer, indent);
		}
		writer.write("}");
	}

	/**
	 * Writes the elements as a pretty JSON object to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeObject(Map, Writer, int)
	 */
	public static void writeObject(Map<String, ? extends Number> elements, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeObject(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON object.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeObject(Map, Writer, int)
	 */
	public static String writeObject(Map<String, ? extends Number> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeObject(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the elements as a pretty JSON object with nested arrays. The generic
	 * notation used allows this method to be used for any type of map with any type
	 * of nested collection of number objects.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param indent   the initial indent level; the first bracket is not indented,
	 *                 inner elements are indented by one, and the last bracket is
	 *                 indented at the initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 * @see #writeArray(Collection)
	 */
	public static void writeObjectArrays(Map<String, ? extends Collection<? extends Number>> elements, Writer writer,
			int indent) throws IOException {
		writer.write("{");
		writer.write("\n");
		var iterator = elements.entrySet().iterator();
		Map.Entry<String, ? extends Collection<? extends Number>> entry = null;
		Collection<? extends Number> values = null;
		String key = null;

		if (iterator.hasNext()) {
			entry = iterator.next();
			key = entry.getKey();
			values = entry.getValue();
			writeIndent(writer, indent + 1);
			writeQuote(key, writer, 0);
			writer.write(": ");
			writeArray(values, writer, indent + 1);
		}

		while (iterator.hasNext()) {
			writer.write(",");
			writer.write("\n");
			entry = iterator.next();
			key = entry.getKey();
			values = entry.getValue();
			writeIndent(writer, indent + 1);
			writeQuote(key, writer, 0);
			writer.write(": ");
			writeArray(values, writer, indent + 1);
		}
		writer.write("\n");
		writeIndent(writer, indent);
		writer.write("}");

	}

	/**
	 * Writes the elements as a pretty JSON object with nested arrays to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeObjectArrays(Map, Writer, int)
	 */
	public static void writeObjectArrays(Map<String, ? extends Collection<? extends Number>> elements, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeObjectArrays(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON object with nested arrays.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeObjectArrays(Map, Writer, int)
	 */
	public static String writeObjectArrays(Map<String, ? extends Collection<? extends Number>> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeObjectArrays(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the elements as a pretty JSON array with nested objects. The generic
	 * notation used allows this method to be used for any type of collection with
	 * any type of nested map of String keys to number objects.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param indent   the initial indent level; the first bracket is not indented,
	 *                 inner elements are indented by one, and the last bracket is
	 *                 indented at the initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 * @see #writeObject(Map)
	 */
	public static void writeArrayObjects(Collection<? extends Map<String, ? extends Number>> elements, Writer writer,
			int indent) throws IOException {
		writer.write("[");
		writer.write("\n");
		var iterator = elements.iterator();
		Map<String, ? extends Number> element = null;

		if (iterator.hasNext()) {
			element = iterator.next();
			writeIndent(writer, indent + 1);
			writeObject(element, writer, indent + 1);
		}

		while (iterator.hasNext()) {
			writer.write(",");
			writer.write("\n");
			element = iterator.next();
			writeIndent(writer, indent + 1);
			writeObject(element, writer, indent + 1);
		}
		writer.write("\n");
		writeIndent(writer, indent);
		writer.write("]");

	}

	/**
	 * Writes the elements as a pretty JSON array with nested objects to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeArrayObjects(Collection)
	 */
	public static void writeArrayObjects(Collection<? extends Map<String, ? extends Number>> elements, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeArrayObjects(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON array with nested objects.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeArrayObjects(Collection)
	 */
	public static String writeArrayObjects(Collection<? extends Map<String, ? extends Number>> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeArrayObjects(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the inverted index as a pretty JSON object
	 *
	 * @param invertedIndex the inverted index to write
	 * @param indexPath     path of the index file
	 * @param indent        the initial indentation level for the JSON output
	 * @throws IOException if an I/O error occurs while writing
	 */
	public static void writeIndex(TreeMap<String, ? extends Map<String, ? extends TreeSet<Integer>>> invertedIndex,
			String indexPath, int indent) throws IOException {
		/*
		 * TODO Try to make this type more generic so that it works with any type of map
		 * and collection and number. Use the other methods as a clue of how to make
		 * this work. The ? extends syntax is important for nested types! Reach out on
		 * Piazza if you run into issues---it is a really hard generic type to get just
		 * right!
		 */

		/*
		 * TODO Notice how all the other methods here have 3 versions? There is the
		 * super general and reusable version that takes a writer and indent level, but
		 * then there are two convenience methods that make common operations (writing
		 * to file or generating a String) more reusable. Try to do the same thing with
		 * your method to output the inverted index!
		 */

		try (BufferedWriter writer = Files.newBufferedWriter(Path.of(indexPath), UTF_8)) {
			writer.write("{");
			writer.write("\n");
			var iterator = invertedIndex.entrySet().iterator();

			if (iterator.hasNext()) {
				writeEntry(iterator.next(), writer, indent + 1);
				while (iterator.hasNext()) {
					writer.write(",");
					writer.write("\n");
					writeEntry(iterator.next(), writer, indent + 1);
				}
				writer.write("\n");
			}
			writeIndent(writer, indent);
			writer.write("}");
		}
	}

	/**
	 * Writes a single entry (word and its corresponding file positions) to the JSON
	 * file.
	 *
	 * @param entry  the entry for a word and its file positions
	 * @param writer the BufferedWriter to write to the JSON file
	 * @param indent the initial indentation level for the JSON output
	 * @throws IOException If an I/O error occurs while writing
	 */
	private static void writeEntry(Map.Entry<String, ? extends Map<String, ? extends TreeSet<Integer>>> entry,
			BufferedWriter writer, int indent) throws IOException {
		String word = entry.getKey();
		Map<String, ? extends TreeSet<Integer>> filePositions = entry.getValue();

		writeIndent(writer, indent);
		writeQuote(word, writer, 0);
		writer.write(": ");
		writeObjectArrays(filePositions, writer, indent);
	}
}