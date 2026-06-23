export interface JiraConnection {
  id?: number;
  connectionName: string;
  jiraBaseUrl: string;
  jiraEmail: string;
  jiraApiToken: string;
  isDefault?: boolean;
  isActive?: boolean;
  lastTestedAt?: string;
  lastTestSuccess?: boolean;
  lastTestMessage?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface JiraConnectionResponse {
  id: number;
  connectionName: string;
  jiraBaseUrl: string;
  jiraEmail: string;
  jiraApiToken: string;
  isDefault: boolean;
  isActive: boolean;
  lastTestedAt?: string;
  lastTestSuccess?: boolean;
  lastTestMessage?: string;
  createdAt: string;
  updatedAt: string;
}
