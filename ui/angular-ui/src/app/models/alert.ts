export interface Alert {
  id: number;
  customerId: number | null;
  accountId: number | null;
  ruleType: string;
  score: number;
  explanation: string;
  evidenceTransactionIds: string;
  status: 'OPEN' | 'IN_REVIEW' | 'CLEARED' | 'ESCALATED';
  createdDate: string;
  updatedDate: string;
}
