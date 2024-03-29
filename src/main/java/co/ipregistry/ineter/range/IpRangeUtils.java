/*
 * Copyright (c) 2020, ineter contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package co.ipregistry.ineter.range;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import co.ipregistry.ineter.base.IpAddress;

abstract class IpRangeUtils {

	static <T> T parseRange(final String from, final BiFunction<String, String, ? extends T> rangeProducer,
			final Function<String, ? extends T> subnetProducer) {
		// The shortest valid string is :: (length 2)
		for (int i = from.length() - 1; i > 1; i--) {
			final char c = from.charAt(i);
			if (c == ',' || c == '-') {
				return rangeProducer.apply(from.substring(0, i).trim(), from.substring(i + 1).trim());
			}
			if (c == '/') {
				return subnetProducer.apply(from.trim());
			}
		}
		final String trimmed = from.trim();
		return rangeProducer.apply(trimmed, trimmed);
	}

	static <T> T parseSubnet(final String from, final BiFunction<String, Integer, ? extends T> subnetProducer,
			final int singleAddressMask) {
		int position = from.length() - 1;
		int charsToCheck = 4; // The slash (/) has to be in the last 4 positions
		while (position > 0 && charsToCheck > 0) {
			if (from.charAt(position) == '/') {
				return subnetProducer.apply(from.substring(0, position).trim(),
						Integer.parseUnsignedInt(from.substring(position + 1).trim()));
			}
			position--;
			charsToCheck--;
		}
		return subnetProducer.apply(from.trim(), singleAddressMask);
	}

	static <L extends Number & Comparable<L>, I extends IpAddress, R extends IpRange<R, ?, I, L>> List<R> merge(
			final Collection<R> rangesToMerge, final BiFunction<I, I, R> rangeCreator) {
		if (rangesToMerge.isEmpty()) {
			return Collections.emptyList();
		}

		final ArrayList<R> sortedRanges = new ArrayList<>(rangesToMerge);
		sortedRanges.sort(Comparator.comparing(R::getFirst));

		int mergedRangeIndex = 0, candidateIndex = 0;
		while (candidateIndex < sortedRanges.size()) {
			// Grab first un-merged range
			R mergedRange = sortedRanges.get(candidateIndex++);
			final I pendingRangeStart = mergedRange.getFirst();
			// extend "mergedRange" as much as possible
			while (candidateIndex < sortedRanges.size()) {
				final R candidateRange = sortedRanges.get(candidateIndex);
				if (!overlapsOrAdjacent(mergedRange, candidateRange)) {
					break;
				}
				mergedRange = rangeCreator.apply(pendingRangeStart,
						IpAddress.max(mergedRange.getLast(), candidateRange.getLast()));
				candidateIndex++;
			}
			sortedRanges.set(mergedRangeIndex++, mergedRange);
		}

		return new ArrayList<>(sortedRanges.subList(0, mergedRangeIndex));
	}

	static <L extends Number & Comparable<L>, I extends IpAddress, R extends IpRange<R, ?, I, L>> boolean overlapsOrAdjacent(
			final R mergedRange, final R candidateRange) {
		return mergedRange.overlaps(candidateRange) || mergedRange.getLast().next().equals(candidateRange.getFirst());
	}
}
