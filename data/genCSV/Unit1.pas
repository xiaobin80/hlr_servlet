unit Unit1;

interface

uses
  Windows, Messages, SysUtils, Variants, Classes, Graphics, Controls, Forms,
  Dialogs, StdCtrls, DB, ADODB;

type
  TForm1 = class(TForm)
    Button1: TButton;
    edtDBPath: TEdit;
    Label1: TLabel;
    ADOConnection1: TADOConnection;
    edtTlbName: TEdit;
    Label2: TLabel;
    edtExpPath: TEdit;
    Label3: TLabel;
    procedure Button1Click(Sender: TObject);
  private
    { Private declarations }
  public
    { Public declarations }
  end;

var
  Form1: TForm1;

implementation
  uses
    ugeneralADO, StrUtils;
{$R *.dfm}

procedure TForm1.Button1Click(Sender: TObject);
var
  adoStr, DBPathStr, tlbNameStr, colNames, expPathStr: String;
  tlbNameLen: Integer;
begin
  DBPathStr:= Trim(edtDBPath.Text);
  adoStr:= 'Provider=Microsoft.Jet.OLEDB.4.0;Data Source=' + DBPathStr
  + ';Persist Security Info=False';
  colNames:= 'id_master,train_no,number,type,'
            + 'lableweight,selfweight,time_report';

  tlbNameStr:= Trim(edtTlbName.Text);
  expPathStr:= Trim(edtExpPath.Text);
  generalCSV(adoStr, colNames, tlbNameStr, expPathStr, 7);
end;

end.
