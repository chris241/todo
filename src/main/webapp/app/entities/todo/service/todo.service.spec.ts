import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { ITodo } from '../todo.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../todo.test-samples';

import { TodoService } from './todo.service';

const requireRestSample: ITodo = {
  ...sampleWithRequiredData,
};

describe('Todo Service', () => {
  let service: TodoService;
  let httpMock: HttpTestingController;
  let expectedResult: ITodo | ITodo[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(TodoService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  describe('Service methods', () => {
    it('should find an element', () => {
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.find(123).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should create a Todo', () => {
      const todo = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(todo).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a Todo', () => {
      const todo = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(todo).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a Todo', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of Todo', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a Todo', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    describe('addTodoToCollectionIfMissing', () => {
      it('should add a Todo to an empty array', () => {
        const todo: ITodo = sampleWithRequiredData;
        expectedResult = service.addTodoToCollectionIfMissing([], todo);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(todo);
      });

      it('should not add a Todo to an array that contains it', () => {
        const todo: ITodo = sampleWithRequiredData;
        const todoCollection: ITodo[] = [
          {
            ...todo,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addTodoToCollectionIfMissing(todoCollection, todo);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a Todo to an array that doesn't contain it", () => {
        const todo: ITodo = sampleWithRequiredData;
        const todoCollection: ITodo[] = [sampleWithPartialData];
        expectedResult = service.addTodoToCollectionIfMissing(todoCollection, todo);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(todo);
      });

      it('should add only unique Todo to an array', () => {
        const todoArray: ITodo[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const todoCollection: ITodo[] = [sampleWithRequiredData];
        expectedResult = service.addTodoToCollectionIfMissing(todoCollection, ...todoArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const todo: ITodo = sampleWithRequiredData;
        const todo2: ITodo = sampleWithPartialData;
        expectedResult = service.addTodoToCollectionIfMissing([], todo, todo2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(todo);
        expect(expectedResult).toContain(todo2);
      });

      it('should accept null and undefined values', () => {
        const todo: ITodo = sampleWithRequiredData;
        expectedResult = service.addTodoToCollectionIfMissing([], null, todo, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(todo);
      });

      it('should return initial array if no Todo is added', () => {
        const todoCollection: ITodo[] = [sampleWithRequiredData];
        expectedResult = service.addTodoToCollectionIfMissing(todoCollection, undefined, null);
        expect(expectedResult).toEqual(todoCollection);
      });
    });

    describe('compareTodo', () => {
      it('Should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareTodo(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('Should return false if one entity is null', () => {
        const entity1 = { id: 123 };
        const entity2 = null;

        const compareResult1 = service.compareTodo(entity1, entity2);
        const compareResult2 = service.compareTodo(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey differs', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 456 };

        const compareResult1 = service.compareTodo(entity1, entity2);
        const compareResult2 = service.compareTodo(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey matches', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 123 };

        const compareResult1 = service.compareTodo(entity1, entity2);
        const compareResult2 = service.compareTodo(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
