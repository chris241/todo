import { IUser } from './user.model';

export const sampleWithRequiredData: IUser = {
  id: 21756,
  login: 'WL',
};

export const sampleWithPartialData: IUser = {
  id: 14655,
  login: '8kfST',
};

export const sampleWithFullData: IUser = {
  id: 28561,
  login: 'uC@V-J\\hj6MXlI\\]lp-wLF\\{QbW\\%bZvV\\j6',
};
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
