export type TransactionType = 'INCOME' | 'EXPENSE';

export interface CategoryResponse {
  id: string;
  name: string;
  type: TransactionType;
}

export interface TransactionResponse {
  id: string;
  amount: number;
  categoryName: string;
  categoryId: string;
  note: string;
  transactionDate: string;
}