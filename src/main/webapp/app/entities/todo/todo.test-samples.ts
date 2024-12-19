import { ITodo, NewTodo } from './todo.model';

export const sampleWithRequiredData: ITodo = {
  id: 32616,
};

export const sampleWithPartialData: ITodo = {
  id: 29388,
  status: 'suivant ressembler consoler',
};

export const sampleWithFullData: ITodo = {
  id: 23393,
  title: 'étant donné que préparer',
  status: 'de façon à ce que saluer',
};

export const sampleWithNewData: NewTodo = {
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
