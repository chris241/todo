import { IAuthority, NewAuthority } from './authority.model';

export const sampleWithRequiredData: IAuthority = {
  name: 'cab8a0d3-8f93-4467-a52d-abaf0a47a694',
};

export const sampleWithPartialData: IAuthority = {
  name: '2973df40-e242-4e43-8e09-be59631a5d33',
};

export const sampleWithFullData: IAuthority = {
  name: 'c6a263e6-a734-4245-baa3-717bb59e3074',
};

export const sampleWithNewData: NewAuthority = {
  name: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
