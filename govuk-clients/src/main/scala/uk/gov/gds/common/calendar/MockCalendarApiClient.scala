package uk.gov.gds.common.calendar

object MockCalendarApiClient extends CalendarApiClient {
  def getBankHolidays =
"""
{
   "england-and-wales":{
      "division":"england-and-wales",
      "calendars":{
         "2012":{
            "year":"2012",
            "division":"england-and-wales",
            "events":[
               {
                  "title":"New Year\u2019s Day",
                  "date":"2012-01-02",
                  "notes":"Substitute day",
                  "bunting":"true"
               },
               {
                  "title":"Good Friday",
                  "date":"2012-04-06",
                  "notes":"",
                  "bunting":"true"
               },
               {
                  "title":"Easter Monday",
                  "date":"2012-04-09",
                  "notes":"",
                  "bunting":"true"
               },
               {
                  "title":"Early May bank holiday",
                  "date":"2012-05-07",
                  "notes":"",
                  "bunting":"true"
               },
               {
                  "title":"Spring bank holiday",
                  "date":"2012-06-04",
                  "notes":"Substitute day",
                  "bunting":"true"
               },
               {
                  "title":"Queen\u2019s Diamond Jubilee",
                  "date":"2012-06-05",
                  "notes":"Extra bank holiday",
                  "bunting":"true"
               },
               {
                  "title":"Summer bank holiday",
                  "date":"2012-08-27",
                  "notes":"",
                  "bunting":"true"
               },
               {
                  "title":"Christmas Day",
                  "date":"2012-12-25",
                  "notes":"",
                  "bunting":"true"
               },
               {
                  "title":"Boxing Day",
                  "date":"2012-12-26",
                  "notes":"",
                  "bunting":"true"
               }
            ]
         },
         "2013":{
            "year":"2013",
            "division":"england-and-wales",
            "events":[
               {
                  "title":"New Year\u2019s Day",
                  "date":"2013-01-01",
                  "notes":"",
                  "bunting":"true"
               },
               {
                  "title":"Good Friday",
                  "date":"2013-03-29",
                  "notes":"",
                  "bunting":"true"
               },
               {
                  "title":"Easter Monday",
                  "date":"2013-04-01",
                  "notes":"",
                  "bunting":"true"
               },
               {
                  "title":"Early May bank holiday",
                  "date":"2013-05-06",
                  "notes":"",
                  "bunting":"true"
               },
               {
                  "title":"Spring bank holiday",
                  "date":"2013-05-27",
                  "notes":"",
                  "bunting":"true"
               },
               {
                  "title":"Summer bank holiday",
                  "date":"2013-08-26",
                  "notes":"",
                  "bunting":"true"
               },
               {
                  "title":"Christmas Day",
                  "date":"2013-12-25",
                  "notes":"",
                  "bunting":"true"
               },
               {
                  "title":"Boxing Day",
                  "date":"2013-12-26",
                  "notes":"",
                  "bunting":"true"
               }
            ]
         }
      }
   },
   "scotland":{
      "division":"scotland",
      "calendars":{
         "2012":{
            "year":"2012",
            "division":"scotland",
            "events":[
               {
                  "title":"2nd January",
                  "date":"2012-01-02",
                  "notes":"",
                  "bunting":"true"
               },
               {
                  "title":"New Year\u2019s Day",
                  "date":"2012-01-03",
                  "notes":"Substitute day",
                  "bunting":"true"
               },
               {
                  "title":"Good Friday",
                  "date":"2012-04-06",
                  "notes":"",
                  "bunting":"true"
               },
               {
                  "title":"Early May bank holiday",
                  "date":"2012-05-07",
                  "notes":"",
                  "bunting":"true"
               },
               {
                  "title":"Spring bank holiday",
                  "date":"2012-06-04",
                  "notes":"Substitute day",
                  "bunting":"true"
               },
               {
                  "title":"Queen\u2019s Diamond Jubilee",
                  "date":"2012-06-05",
                  "notes":"Extra bank holiday",
                  "bunting":"true"
               },
               {
                  "title":"Summer bank holiday",
                  "date":"2012-08-06",
                  "notes":"",
                  "bunting":"true"
               },
               {
                  "title":"St Andrew\u2019s Day",
                  "date":"2012-11-30",
                  "notes":"",
                  "bunting":"true"
               },
               {
                  "title":"Christmas Day",
                  "date":"2012-12-25",
                  "notes":"",
                  "bunting":"true"
               },
               {
                  "title":"Boxing Day",
                  "date":"2012-12-26",
                  "notes":"",
                  "bunting":"true"
               }
            ]
         },
         "2013":{
            "year":"2013",
            "division":"scotland",
            "events":[
               {
                  "title":"New Year\u2019s Day",
                  "date":"2013-01-01",
                  "notes":"",
                  "bunting":"true"
               },
               {
                  "title":"2nd January",
                  "date":"2013-01-02",
                  "notes":"",
                  "bunting":"true"
               },
               {
                  "title":"Good Friday",
                  "date":"2013-03-29",
                  "notes":"",
                  "bunting":"true"
               },
               {
                  "title":"Early May bank holiday",
                  "date":"2013-05-06",
                  "notes":"",
                  "bunting":"true"
               },
               {
                  "title":"Spring bank holiday",
                  "date":"2013-05-27",
                  "notes":"",
                  "bunting":"true"
               },
               {
                  "title":"Summer bank holiday",
                  "date":"2013-08-05",
                  "notes":"",
                  "bunting":"true"
               },
               {
                  "title":"St Andrew\u2019s Day",
                  "date":"2013-12-02",
                  "notes":"Substitute day",
                  "bunting":"true"
               },
               {
                  "title":"Christmas Day",
                  "date":"2013-12-25",
                  "notes":"",
                  "bunting":"true"
               },
               {
                  "title":"Boxing Day",
                  "date":"2013-12-26",
                  "notes":"",
                  "bunting":"true"
               }
            ]
         }
      }
   },
   "ni":{
      "division":"ni",
      "calendars":{
         "2012":{
            "year":"2012",
            "division":"ni",
            "events":[
               {
                  "title":"New Year\u2019s Day",
                  "date":"2012-01-02",
                  "notes":"Substitute day",
                  "bunting":"true"
               },
               {
                  "title":"St Patrick\u2019s Day",
                  "date":"2012-03-19",
                  "notes":"Substitute day",
                  "bunting":"true"
               },
               {
                  "title":"Good Friday",
                  "date":"2012-04-06",
                  "notes":"",
                  "bunting":"true"
               },
               {
                  "title":"Easter Monday",
                  "date":"2012-04-09",
                  "notes":"",
                  "bunting":"true"
               },
               {
                  "title":"Early May bank holiday",
                  "date":"2012-05-07",
                  "notes":"",
                  "bunting":"true"
               },
               {
                  "title":"Spring bank holiday",
                  "date":"2012-06-04",
                  "notes":"",
                  "bunting":"true"
               },
               {
                  "title":"Queen\u2019s Diamond Jubilee",
                  "date":"2012-06-05",
                  "notes":"Extra bank holiday",
                  "bunting":"true"
               },
               {
                  "title":"Battle of the Boyne (Orangemen\u2019s Day)",
                  "date":"2012-07-12",
                  "notes":"",
                  "bunting":"false"
               },
               {
                  "title":"Summer bank holiday",
                  "date":"2012-08-27",
                  "notes":"",
                  "bunting":"true"
               },
               {
                  "title":"Christmas Day",
                  "date":"2012-12-25",
                  "notes":"",
                  "bunting":"true"
               },
               {
                  "title":"Boxing Day",
                  "date":"2012-12-26",
                  "notes":"",
                  "bunting":"true"
               }
            ]
         },
         "2013":{
            "year":"2013",
            "division":"ni",
            "events":[
               {
                  "title":"New Year\u2019s Day",
                  "date":"2013-01-01",
                  "notes":"",
                  "bunting":"true"
               },
               {
                  "title":"St Patrick\u2019s Day",
                  "date":"2013-03-18",
                  "notes":"Substitute day",
                  "bunting":"true"
               },
               {
                  "title":"Good Friday",
                  "date":"2013-03-29",
                  "notes":"",
                  "bunting":"true"
               },
               {
                  "title":"Easter Monday",
                  "date":"2013-04-01",
                  "notes":"",
                  "bunting":"true"
               },
               {
                  "title":"Early May bank holiday",
                  "date":"2013-05-06",
                  "notes":"",
                  "bunting":"true"
               },
               {
                  "title":"Spring bank holiday",
                  "date":"2013-05-27",
                  "notes":"",
                  "bunting":"true"
               },
               {
                  "title":"Battle of the Boyne (Orangemen\u2019s Day)",
                  "date":"2013-07-12",
                  "notes":"",
                  "bunting":"false"
               },
               {
                  "title":"Summer bank holiday",
                  "date":"2013-08-26",
                  "notes":"",
                  "bunting":"true"
               },
               {
                  "title":"Christmas Day",
                  "date":"2013-12-25",
                  "notes":"",
                  "bunting":"true"
               },
               {
                  "title":"Boxing Day",
                  "date":"2013-12-26",
                  "notes":"",
                  "bunting":"true"
               }
            ]
         }
      }
   }
}
"""
}
