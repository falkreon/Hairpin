package com.thoughtcomplex.hairpin

import com.thoughtcomplex.hairpin.ClassPattern

/**
 * Created by Falkreon on 7/3/2014.
 */
public interface Mapper {
	public Map<String, List<ClassPattern>> getPatterns();
	public Map<String, List<ClassPattern>> getIndirectPatterns();
}