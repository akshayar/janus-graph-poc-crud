package com.aksh.titan.service;

public interface Transactional<T> {
	T run();
}

