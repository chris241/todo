import { IUser } from 'app/entities/user/user.model';

export interface ITodo {
  id: number;
  title?: string | null;
  status?: string | null;
  user?: Pick<IUser, 'id' | 'login'> | null;
}

export type NewTodo = Omit<ITodo, 'id'> & { id: null };
