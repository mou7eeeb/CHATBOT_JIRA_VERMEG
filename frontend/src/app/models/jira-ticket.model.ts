export interface JiraTicket {
  key: string;
  id: string;
  fields: TicketFields;
}

export interface TicketFields {
  summary: string;
  description?: string;
  status: StatusInfo;
  issuetype: IssueTypeInfo;
  priority?: PriorityInfo;
  assignee?: AssigneeInfo;
  reporter?: ReporterInfo;
  created: string;
  updated: string;
}

export interface StatusInfo {
  name: string;
  statusCategory?: {
    key: string;
    colorName: string;
  };
}

export interface IssueTypeInfo {
  name: string;
  iconUrl?: string;
}

export interface PriorityInfo {
  name: string;
  iconUrl?: string;
}

export interface AssigneeInfo {
  displayName: string;
  emailAddress?: string;
  avatarUrls?: any;
}

export interface ReporterInfo {
  displayName: string;
  emailAddress?: string;
}
