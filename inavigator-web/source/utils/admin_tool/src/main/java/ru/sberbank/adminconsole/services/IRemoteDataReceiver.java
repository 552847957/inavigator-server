package ru.sberbank.adminconsole.services;

public interface IRemoteDataReceiver<T> {
	void submit(T xml);
}
