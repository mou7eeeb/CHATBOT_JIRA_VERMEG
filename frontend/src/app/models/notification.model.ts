export interface Notification {
  id: number;
  message: string;
  read: boolean;
  chatSessionId?: number;
  createdAt: string;
}
