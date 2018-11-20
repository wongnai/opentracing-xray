package com.wongnai.common;

import java.util.Stack;

/**
 * Local stack.
 *
 * @author Suparit Krityakien
 *
 * @param <T>
 *            Type of element
 */
public class LocalStack<T> {
	private ThreadLocal<Stack<T>> localStack = ThreadLocal.withInitial(() -> new Stack<T>());

	/**
	 * Gets top of stack.
	 *
	 * @return top of stack
	 */
	public T get() {
		return localStack.get().peek();
	}

	/**
	 * Checks if stack is empty or not.
	 *
	 * @return {@code true} if stack is empty
	 */
	public boolean isEmpty() {
		return localStack.get().isEmpty();
	}

	/**
	 * Pushes element to stack.
	 *
	 * @param element
	 *            element
	 */
	public void push(T element) {
		localStack.get().push(element);
	}

	/**
	 * Pops top element out from stack.
	 *
	 * @return top element
	 */
	public T pop() {
		Stack<T> stack = localStack.get();
		if (!stack.isEmpty()) {
			return stack.pop();
		} else {
			return null;
		}
	}

	/**
	 * Clears local thread storage.
	 */
	public void remove() {
		localStack.remove();
	}

	/**
	 * Clears local thread storage if empty.
	 */
	public void removeIfEmpty() {
		if (localStack.get().isEmpty()) {
			localStack.remove();
		}
	}
}
