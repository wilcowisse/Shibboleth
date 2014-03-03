CREATE TABLE "Chunks" (
  "id" INTEGER PRIMARY KEY,
  "file_id" int(11) NOT NULL,
  "start" int(11) NOT NULL,
  "end" int(11) NOT NULL,
  "committer_id" int(11) NOT NULL,
  "time" varchar(64) NOT NULL
);
CREATE TABLE "Committers" (
  "id" INTEGER PRIMARY KEY,
  "repo" varchar(255) NOT NULL,
  "email" varchar(255) NOT NULL,
  "name" varchar(255) NOT NULL
);
CREATE TABLE "ContributionInfo" (
  "contribution_id" int(11) PRIMARY KEY NOT NULL,
  "count" int(11) NOT NULL,
  "percentage" int(11) NOT NULL
);
CREATE TABLE "Contributions" (
  "id" INTEGER PRIMARY KEY,
  "repo_name" varchar(255) NOT NULL,
  "user_name" varchar(255) NOT NULL
);
CREATE TABLE "Files" (
  "id" INTEGER PRIMARY KEY,
  "repo" varchar(255) NOT NULL,
  "head" varchar(40) NOT NULL,
  "file_path" varchar(255) NOT NULL
);
CREATE TABLE "RecordLinks" (
  "committer" int(11) PRIMARY KEY NOT NULL,
  "user" varchar(255) NOT NULL
);
CREATE TABLE "Repos" (
  "id" INTEGER PRIMARY KEY NOT NULL,
  "full_name" varchar(255) NOT NULL,
  "owner" varchar(255) NOT NULL,
  "url" varchar(255) NOT NULL,
  "clone_url" varchar(255) NOT NULL,
  "parent" varchar(255) NOT NULL,
  "fork" tinyint(1) NOT NULL,
  "forks_count" int(11) NOT NULL,
  "size" int(11) NOT NULL,
  "language" varchar(255) NOT NULL
);
CREATE TABLE "StoredLinks" (
  "name" varchar(255) PRIMARY KEY NOT NULL,
  "type" varchar(64) NOT NULL
);
CREATE TABLE "Users" (
  "id" int(11) PRIMARY KEY NOT NULL,
  "login" varchar(255) NOT NULL,
  "name" varchar(255) NOT NULL,
  "email" varchar(255) NOT NULL,
  "url" varchar(255) NOT NULL,
  "type" varchar(64) NOT NULL,
  "company" varchar(255) NOT NULL,
  "repos" int(11) NOT NULL,
  "followers" int(11) NOT NULL,
  "following" int(11) NOT NULL
);
CREATE INDEX "Chunks_id" ON "Chunks" ("id");
CREATE INDEX "Committers_id" ON "Committers" ("id");
CREATE INDEX "ContributionInfo_contribution_id" ON "ContributionInfo" ("contribution_id");
CREATE INDEX "Contributions_id" ON "Contributions" ("id");
CREATE INDEX "Files_id" ON "Files" ("id");
CREATE INDEX "RecordLinks_committer" ON "RecordLinks" ("committer");
CREATE INDEX "Repos_full_name" ON "Repos" ("full_name");
CREATE INDEX "Repos_id" ON "Repos" ("id");
CREATE INDEX "StoredLinks_name" ON "StoredLinks" ("name");
CREATE INDEX "Users_login" ON "Users" ("login");

