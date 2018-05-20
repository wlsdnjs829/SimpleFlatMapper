package org.simpleflatmapper.map;


public interface Mapper<S, T> extends FieldMapper<S, T> {
	/**
	 * map source object to a new newInstance of T.
	 * This method does not manage the iteration of the source and will only map the current row. No join aggregation will be performed.
	 * @param source object to map from
	 * @return a new mapped newInstance of T
	 * @throws MappingException if an exception occurs
	 */
    T map(S source) throws MappingException;

	/**
	 * map source object to a new newInstance of T.
	 * This method does not manage the iteration of the source and will only map the current row. No join aggregation will be performed.
	 * @param source object to map from
	 * @return a new mapped newInstance of T
	 * @throws MappingException if an exception occurs
	 */
    T map(S source, MappingContext<? super S> context) throws MappingException;
}