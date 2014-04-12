package com.github.eclipse.projectlocationupdater.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Helper class for list operations.
 *
 * @author Max Gensthaler
 */
public abstract class ListUtil {
	private ListUtil() {
		//
	}

	public static <E> List<E> filterToList(Collection<E> input, Predicate<? super E> predicate) {
		return filterTo(input, predicate, new ArrayList<E>());
	}

	public static <E> Set<E> filterToSet(Collection<E> input, Predicate<? super E> predicate) {
		return filterTo(input, predicate, new HashSet<E>());
	}

	private static <E, C extends Collection<E>> C filterTo(Collection<E> input, Predicate<? super E> predicate, C resultCollection) {
		for (E element : input) {
			if (predicate.apply(element)) {
				resultCollection.add(element);
			}
		}
		return resultCollection;
	}
}
