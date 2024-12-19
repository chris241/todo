import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import TodoResolve from './route/todo-routing-resolve.service';

const todoRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/todo.component').then(m => m.TodoComponent),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/todo-detail.component').then(m => m.TodoDetailComponent),
    resolve: {
      todo: TodoResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/todo-update.component').then(m => m.TodoUpdateComponent),
    resolve: {
      todo: TodoResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/todo-update.component').then(m => m.TodoUpdateComponent),
    resolve: {
      todo: TodoResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default todoRoute;
