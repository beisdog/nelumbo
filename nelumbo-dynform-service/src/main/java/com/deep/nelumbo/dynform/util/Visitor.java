package com.deep.nelumbo.dynform.util;


/**
 * Simple visitor interface used to walk a treestructure or a list.
 * Used in {@link com.deep.nelumbo.dynform.dto.DynFormDataNodeDTO#traverseFieldsRecursively(Visitor)}
 * @author X200531
 *
 * @param <T>
 */
public interface Visitor<T> {
	/**
	 * Callback method for each element this interface visits.
	 * @param t
	 * @return continue to visit. If false then stop
	 */
	boolean visit(T element);

}
