<a href="https://career.softserveinc.com/en-us/technology/course/start_your_career_as_java_developer/"><img src="https://github.com/ita-social-projects/GreenCity/blob/master/docs-photos/GreenCity%20Logo.png" title="SoftServe IT Academy. GreenCity project" alt="SoftServe IT Academy. GreenCity project"></a>


# GreenCity    [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://github.com/ita-social-projects/GreenCity/blob/master/LICENSE) [![GitHub release](https://img.shields.io/static/v1?label=Pre-release&message=v.3.0.0&color=yellowgreen)](https://github.com/ita-social-projects/GreenCity/releases/) [![Build Status](https://travis-ci.com/ita-social-projects/GreenCity.svg?branch=master)](https://travis-ci.com/ita-social-projects/GreenCity) [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=ita-social-projects-green-city&metric=coverage)](https://sonarcloud.io/dashboard?id=ita-social-projects-green-city) [![Github Issues](https://img.shields.io/github/issues/ita-social-projects/GreenCity?style=flat-square)](https://github.com/ita-social-projects/GreenCity/issues) [![Pending Pull-Requests](https://img.shields.io/github/issues-pr/ita-social-projects/GreenCity?style=flat-square)](https://github.com/ita-social-projects/GreenCity/pulls)

**Copyright 2020 Softserve IT Academy**

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.


## 1. About the project

The main aim of “GreenCity” project is to teach people in a playful and challenging way to have an eco-friendly lifestyle. A user can view on the map places that have some eco-initiatives or suggest discounts for being environmentally aware (for instance, coffee shops that give a discount if a customer comes with their own cup). А user can start doing an environment-friendly habit and track their progress with a habit tracker.

## 2. Where to find front-end part of the project

Here is the front-end part of our project: https://github.com/ita-social-projects/GreenCityClient.

`dev` branch of the back-end corresponds to `dev` branch on the front-end. The same thing with `master` branches.

## 3. How to contribute

You're encouraged to contribute to our project if you've found any issues or missing functionality that you would want to see. Here you can see [the list of issues](https://github.com/ita-social-projects/GreenCity/issues) and here you can create [a new issue](https://github.com/ita-social-projects/GreenCity/issues/new).

Before sending any pull request, please discuss requirements/changes to be implemented using an existing issue or by creating a new one. All pull requests should be done into `dev` branch.

Though there are two GitHub projects ([GreenCity](https://github.com/ita-social-projects/GreenCity) for back-end part and [GreenCityClient](https://github.com/ita-social-projects/GreenCityClient) for front-end part) all of the issues are listed in the first one - [GreenCity](https://github.com/ita-social-projects/GreenCity).

**NOTE: make sure that your code passes checkstyle. Otherwise your pull request will be declined**. See paragraph [Setup Checkstyle](#5-setup-checkstyle).

## 4. Start the project locally

### 4.1. Required to install

* Java 21
* PostgreSQL 9.5 or higher

### 4.2. How to run

1. You should open in IntelliJ IDEA File -> `New Project` -> `Project From Version Control`
   -> `Repository URL` -> `URL` (https://github.com/ita-social-projects/GreenCity.git) -> `Clone`.


2. Open `Terminal` write `git checkout -b dev` (this will create new local branch "dev").


3. After this `git pull origin dev` (for update last version from branch dev)


4. You should create database `greencity`.


5. `Add Configuration` -> `+` -> `Application`.

* `Name` : `GreenCityApplication`.
* `Use classpath of modules`:`core`
* `JRE` : `21`.

6. `Enviroment variables`:

![env-vars](./docs-photos/env-example.png)

7. Go to `dao` -> `src` -> `test` -> `resources` -> `sql`, find file `insert.sql`,
   open it, press `Ctrl + A` -> `RMB` -> `Execute`. (that will run script which fill your db)


8. `Run Application`


9. If you did everything correctly, you should be able to access swagger by this URL: http://localhost:8080/swagger-ui.html#/

### 4.3. How to work with swagger UI in our project

1. Run GreenCity project (look up paragraph [How to run](#42-how-to-run)).

2. Use the following link to open Swagger UI: http://localhost:8080/swagger-ui.html#/

3. Use POST method with `/ownSecurity/signUp` to create an account. If you set a valid email credentials, you should receive an email with verification link. Verify the registration by following that link. We highly recommend to use gmail, it's free of charge and easy to get going: [how to allow email sending from gmail](https://support.google.com/accounts/answer/6010255?authuser=2&p=less-secure-apps&hl=en&authuser=2&visit_id=637098532320915318-4087823934&rd=1),  [Google client id](https://developers.google.com/adwords/api/docs/guides/authentication). Alternatively you can drop a record in `verify_email` table on your local database.

4. Use POST method with `/ownSecurity/signIn` to sign in. After entering the credentials you should receive access and refresh tokens. 

5. Copy the given access token and put it into Authentication Header. Press **Authorize** button.

   ![Authentication-button-swagger](./docs-photos/authentication-swagger.png)

   Insert the given token into input field. The scheme should be like this `<given_token>`. Press **Authorize** button.

   ![Bearer-examle](./docs-photos/auth.png)

6. Now you can use swagger UI to test REST API. Some controllers require *ADMIN* role. By default, new registered users have role *USER*. To overcome this you need to update record that corresponds to your user in the local database. For example, `UPDATE users SET role = 1 WHERE id = <your_user_id>`.

### 4.4. Connect with front-end

There is no special configurations required. Just clone [GreenCityClient](https://github.com/ita-social-projects/GreenCityClient) and run it. If you want to sign in with Google account, it's mandatory to set `google.clientId`. Read more about how to obtain [Google client id](https://developers.google.com/adwords/api/docs/guides/authentication), it's free.

## 5. Setup Checkstyle

Here you can read more about [how to set up checkstyle](https://github.com/ita-social-projects/GreenCity/wiki/Setup-CheckStyle-to-your-IDE);

Here you can read more about [SonarLint](https://plugins.jetbrains.com/plugin/7973-sonarlint);
Test
