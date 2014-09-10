package com.thoughtcomplex.hairpin

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Created by Falkreon on 7/9/2014.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public abstract @interface Plugin {
	String id();
	String name();
	double version();
	String author();
}
