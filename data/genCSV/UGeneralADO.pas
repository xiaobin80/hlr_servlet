unit UGeneralADO;

interface
uses
  SysUtils, Classes, ADODB, StrUtils;

var
  saveFilePath:WideString;

function generalCSV(ADOconnectString,ColName,TableName1,ExprotFileName1:WideString;ColCount:integer):Boolean;

implementation

function generalCSV(ADOconnectString,ColName,TableName1,ExprotFileName1:WideString;ColCount:integer):Boolean;
var
  ADOConnectX:TADOConnection;
  ADOQueryX:TADOQuery;
  
  TempStr:WideString;
  iFor: integer;
  TempList:TStringList;
  //
  tempStr2: WideString;
  year_level2Str: WideString;
  month_level3Str: WideString;
begin
  ADOConnectX:=TADOConnection.Create(nil);
  ADOQueryX:=TADOQuery.Create(nil);

  ADOConnectX.LoginPrompt:=False;
  ADOConnectX.Close;
  ADOConnectX.ConnectionString:=ADOconnectString;
  ADOConnectX.Open;
  
  ADOQueryX.Connection:=ADOConnectX;
  ADOQueryX.Close;
  ADOQueryX.SQL.Text:='select '+ColName+' from '+TableName1;
  ADOQueryX.Open;


  TempStr := '';
  TempList := TStringList.Create;
  ADOQueryX.First;
  while not ADOQueryX.Eof do
  begin
    TempStr := '';
    for iFor := 0 to ColCount-1 do//colcount¡–
    begin
      if iFor=0 then
      begin
        TempStr := TempStr + ADOQueryX.Fields[iFor].AsString;
      end
      else
      begin
        case iFor of
          2..3: TempStr := TempStr +', '''
                          + ADOQueryX.Fields[iFor].AsString + '''';
          6:
            begin
              TempStr := TempStr +', '''
                          + ADOQueryX.Fields[iFor].AsString + '''';
              year_level2Str:= LeftStr(ADOQueryX.Fields[iFor].AsString, 4);
              month_level3Str:= IntToStr(StrToInt(copy(ADOQueryX.Fields[iFor].AsString, 6, 2)));
              TempStr:= TempStr + ', ''' + year_level2Str + ''', ''' + month_level3Str + ''''; 
            end;
          else TempStr := TempStr +','+ ADOQueryX.Fields[iFor].AsString;
        end;

      end;
    end;
    tempStr2:= 'insert into trainOrder(train_number,seriary_number,car_number,car_marque,'
            +'carry_weight1,self_weight1,past_time,year_level2,month_level3) values(';
    TempList.Append(tempStr2 + TempStr + ');');
    ADOQueryX.Next;
  end;

  saveFilePath:=ExprotFileName1;
  TempList.SaveToFile(saveFilePath);
  //free object
  FreeAndNil(TempList);
  FreeAndNil(ADOConnectX);
  FreeAndNil(ADOQueryX);

  Result:=True;

end;

end.
