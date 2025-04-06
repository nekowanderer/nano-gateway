# Quick Notes for Realm Creation in Keycloak Web Admin Console

## Quick Summary
- Suggested Setup Sequence:
  - Realm -> Role -> User -> Client
- The following example is just for the simplest API client with Role-based access control (RBAC).

## Setup Details
- Role
  - Role name 
- User
  - Username
  - email
  - First name
  - Last name
  - Credential
  - Role mapping
- Client
  - Client ID
  - Valid redirect URIs
  - Valid post logout redirect URIs
  - Client authentication (Suggest turning it on)
  - Authorization (Suggest turning it on)
  - Authentication flow
    - Standard flow
    - Direct access grants
    - Service accounts roles
  - Credentials (Default it OK)
  - Authorization
    - Resources
      - Name
      - Display name
      - URIs (Necessary if you want to protect RESTful resources)
    - Policies
      - Name 
      - Roles
    - Permissions
      - Name
      - Resource
      - Policies

## Export Realm
- [Please refer to this article](https://simonscholz.dev/tutorials/keycloak-realm-export-import)
