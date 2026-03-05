using {cuid, managed} from '@sap/cds/common';

entity Employee : cuid, managed {
  @mandatory
  name : String(100);

  @mandatory
  @assert.unique
  email : String(100);

  @mandatory
  department : String(50);

  jobTitle : String(50);
  dateOfJoining : Date;

  manager : association to Employee;
  directReports : composition of many Employee on directReports.manager = $self; // Self-referential composition

  salary : Decimal(15,2);

  status : EmployeeStatus default 'Active';

  projectAssignment : Composition of many ProjectTeamMember on projectAssignment.employee = $self;

}

type EmployeeStatus : String enum {
  Active;
  Inactive;
  OnLeave;
}

entity Project : cuid, managed {
  @mandatory
  projectName : String(100);
  description : String(255);
  @mandatory
  startDate : Date;
  endDate : Date;
  budget : Decimal(15,2);
  projectOwner : association to Employee;
  teamMembers : Composition of many ProjectTeamMember on teamMembers.project = $self;
}

entity ProjectTeamMember : cuid, managed {
  @mandatory
  project : association to Project;

  @mandatory
  employee : association to Employee;

  role : String(50);

}



