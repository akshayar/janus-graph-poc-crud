package com.aksh.janusgraph.service;

public interface Transactional<T> {
	T run();
}

